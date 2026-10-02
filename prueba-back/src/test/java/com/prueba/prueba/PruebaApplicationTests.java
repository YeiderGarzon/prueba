package com.prueba.prueba;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:contexttest;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"app.security.jwt-secret=integration-test-secret-key-at-least-32-bytes"
})
class PruebaApplicationTests {

	@Test
	void contextLoads() {
	}

}
