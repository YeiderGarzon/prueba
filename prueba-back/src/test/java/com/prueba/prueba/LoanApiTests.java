package com.prueba.prueba;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prueba.prueba.application.port.out.LoanQueryCache;
import com.prueba.prueba.application.port.out.PasswordHasher;
import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.infrastructure.persistence.SpringDataLoanRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:loanstest;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"app.security.jwt-secret=integration-test-secret-key-at-least-32-bytes"
})
@AutoConfigureMockMvc
class LoanApiTests {
	private static final String ADMIN_PASSWORD = "test-admin-password-123";
	private static final String CUSTOMER_PASSWORD = "customer-password-123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SpringDataLoanRepository loans;

	@Autowired
	private UserStore users;

	@Autowired
	private PasswordHasher passwordHasher;

	@Autowired
	private LoanQueryCache loanQueryCache;

	private String applicant;
	private String customerToken;
	private String adminToken;

	@BeforeEach
	void registerAndAuthenticateUsers() throws Exception {
		loans.deleteAll();
		loanQueryCache.invalidateAll();
		if (users.findByUsername("test-admin").isEmpty()) {
			users.save(new AppUser(
					null,
					"test-admin",
					"Test Administrator",
					passwordHasher.hash(ADMIN_PASSWORD),
					UserRole.ADMIN));
		}
		applicant = "customer" + System.nanoTime();
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","fullName":"Customer Full Name","password":"%s","role":"ADMIN"}
								""".formatted(applicant, CUSTOMER_PASSWORD)))
				.andExpect(status().isCreated());
		org.junit.jupiter.api.Assertions.assertEquals(
				UserRole.CUSTOMER,
				users.findByUsername(applicant).orElseThrow().getRole());
		customerToken = login(applicant, CUSTOMER_PASSWORD);
		adminToken = login("test-admin", ADMIN_PASSWORD);
	}

	@Test
	void authenticatesAndEnforcesLoanRolesAndOwnership() throws Exception {
		mockMvc.perform(get("/api/loans"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").isNotEmpty());
		mockMvc.perform(patch("/api/loans/{id}/decision", 1)
						.header("Authorization", "Bearer invalid-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"APPROVED"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
		mockMvc.perform(post("/api/loans")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":2500000,"termMonths":24}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.detail").value("No tienes permisos para realizar esta operación."));

		String approvedId = createLoan(customerToken, 2500000);
		mockMvc.perform(get("/api/loans")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].applicantId")
						.value(users.findByUsername(applicant).orElseThrow().getId()))
				.andExpect(jsonPath("$[0].applicantUsername").value(applicant))
				.andExpect(jsonPath("$[0].applicantFullName").value("Customer Full Name"))
				.andExpect(jsonPath("$[0].status").value("PENDING"));

		String otherCustomer = applicant + "x";
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","fullName":"Other Customer","password":"%s"}
								""".formatted(otherCustomer, CUSTOMER_PASSWORD)))
				.andExpect(status().isCreated());
		String otherToken = login(otherCustomer, CUSTOMER_PASSWORD);
		mockMvc.perform(get("/api/loans/{id}", approvedId)
						.header("Authorization", bearer(otherToken)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Préstamo no encontrado"));
		mockMvc.perform(patch("/api/loans/{id}/decision", approvedId)
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"APPROVED"}
								"""))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/loans")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(patch("/api/loans/{id}/decision", approvedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"APPROVED"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));
		mockMvc.perform(get("/api/loans")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("APPROVED"));
		mockMvc.perform(get("/api/loans")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("APPROVED"));
		mockMvc.perform(get("/api/loans/{id}", approvedId)
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));
		mockMvc.perform(patch("/api/loans/{id}/decision", approvedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"REJECTED"}
								"""))
				.andExpect(status().isConflict());

		String rejectedId = createLoan(customerToken, 500000);
		mockMvc.perform(patch("/api/loans/{id}/decision", rejectedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"REJECTED"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"));
	}

	@Test
	void rejectsInvalidRegistrationAndLoanRequests() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"anotheruser","fullName":"Another User","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("La solicitud contiene datos no válidos."))
				.andExpect(jsonPath("$.fieldErrors.password").isNotEmpty());
		mockMvc.perform(post("/api/loans")
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":10,"termMonths":0}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.amount").isNotEmpty())
				.andExpect(jsonPath("$.fieldErrors.termMonths").isNotEmpty());
		mockMvc.perform(post("/api/loans")
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{invalid-json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail")
						.value("El cuerpo de la solicitud no es válido o está mal formado."));
		mockMvc.perform(get("/api/auth/me")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(applicant))
				.andExpect(jsonPath("$.fullName").value("Customer Full Name"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
		mockMvc.perform(get("/api/auth/me")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","fullName":"Customer Full Name","password":"wrong-password"}
								""".formatted(applicant)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void authenticatesAdministratorUsingRoleStoredInDatabase() throws Exception {
		login("test-admin", ADMIN_PASSWORD);
	}

	@Test
	void supportsLoanCrudOnlyWhilePendingAndForItsOwnerOrAnAdministrator() throws Exception {
		String loanId = createLoan(customerToken, 2500000);
		mockMvc.perform(put("/api/loans/{id}", loanId)
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":3000000,"termMonths":36}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(3000000))
				.andExpect(jsonPath("$.termMonths").value(36))
				.andExpect(jsonPath("$.status").value("PENDING"));

		mockMvc.perform(delete("/api/loans/{id}", loanId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/loans/{id}", loanId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isNotFound());

		String resolvedId = createLoan(customerToken, 2500000);
		mockMvc.perform(patch("/api/loans/{id}/decision", resolvedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"APPROVED\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(put("/api/loans/{id}", resolvedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\":3000000,\"termMonths\":36}"))
				.andExpect(status().isConflict());
		mockMvc.perform(delete("/api/loans/{id}", resolvedId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isConflict());
	}

	@Test
	void supportsAdminUserCrudWithoutExposingPasswordsOrOrphaningLoans() throws Exception {
		String applicantId = users.findByUsername(applicant).orElseThrow().getId().toString();
		mockMvc.perform(get("/api/users")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/users/{id}", applicantId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(applicant))
				.andExpect(jsonPath("$.fullName").value("Customer Full Name"))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		MvcResult created = mockMvc.perform(post("/api/users")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"managed-user","fullName":"Managed User","password":"managed-password-123","role":"ADMIN"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.fullName").value("Managed User"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();
		@SuppressWarnings("deprecation")
		String managedId = objectMapper.readTree(created.getResponse().getContentAsString())
				.get("id").asText();
		String managedToken = login("managed-user", "managed-password-123");
		mockMvc.perform(put("/api/users/{id}", managedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"managed-user","fullName":"Managed User","role":"CUSTOMER"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("managed-user"));
		mockMvc.perform(get("/api/users")
						.header("Authorization", bearer(managedToken)))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/users/{id}", managedId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"managed-renamed","fullName":"Managed User Updated","role":"CUSTOMER"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("managed-renamed"))
				.andExpect(jsonPath("$.fullName").value("Managed User Updated"));
		mockMvc.perform(delete("/api/users/{id}", managedId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isNoContent());

		createLoan(customerToken, 2500000);
		mockMvc.perform(put("/api/users/{id}", applicantId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"renamed-applicant","fullName":"Customer Name Updated","role":"CUSTOMER"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("renamed-applicant"));
		String renamedToken = login("renamed-applicant", CUSTOMER_PASSWORD);
		mockMvc.perform(get("/api/loans")
						.header("Authorization", bearer(renamedToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].applicantId").value(applicantId));
		mockMvc.perform(delete("/api/users/{id}", applicantId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isConflict());
	}

	@Test
	void letsAuthenticatedUserEditOnlyTheirOwnProfile() throws Exception {
		String applicantId = users.findByUsername(applicant).orElseThrow().getId().toString();
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(applicant))
				.andExpect(jsonPath("$.fullName").value("Customer Full Name"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
		mockMvc.perform(get("/api/users")
						.header("Authorization", bearer(customerToken)))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/users/me")
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"profile-renamed","fullName":"Changed Full Name","password":"short"}
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(put("/api/users/me")
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"profile-renamed","fullName":"Changed Full Name","password":"","role":"ADMIN"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("profile-renamed"))
				.andExpect(jsonPath("$.fullName").value("Changed Full Name"))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
		org.junit.jupiter.api.Assertions.assertEquals(
				applicantId,
				users.findByUsername("profile-renamed").orElseThrow().getId().toString());
		mockMvc.perform(get("/api/users/me")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@SuppressWarnings("deprecation")
	private String createLoan(String token, int amount) throws Exception {
		MvcResult pendingResult = mockMvc.perform(post("/api/loans")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":%d,"termMonths":24}
								""".formatted(amount)))
				.andExpect(request().asyncStarted())
				.andReturn();
		MvcResult result = mockMvc.perform(asyncDispatch(pendingResult))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andReturn();
		JsonNode loan = objectMapper.readTree(result.getResponse().getContentAsString());
		return loan.get("id").asText();
	}

	@SuppressWarnings("deprecation")
	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"%s"}
								""".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString())
				.get("accessToken").asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}
