package com.prueba.prueba.infrastructure.web;

import com.prueba.prueba.application.exception.ApplicationException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApplicationExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException exception) {
		HttpStatus status = switch (exception.getKind()) {
			case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
			case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
			case NOT_FOUND -> HttpStatus.NOT_FOUND;
			case CONFLICT -> HttpStatus.CONFLICT;
		};
		return error(status, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new TreeMap<>();
		exception.getBindingResult().getFieldErrors().forEach(fieldError ->
				fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
		return validationError(fieldErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> fieldErrors = new TreeMap<>();
		exception.getConstraintViolations().forEach(violation ->
				fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
		return validationError(fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleUnreadableMessage() {
		return error(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido o está mal formado.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception) {
		return error(HttpStatus.BAD_REQUEST,
				"El parámetro '%s' tiene un formato no válido.".formatted(exception.getName()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMethodNotSupported() {
		return error(HttpStatus.METHOD_NOT_ALLOWED, "El método HTTP no está permitido para este recurso.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation() {
		return error(HttpStatus.CONFLICT,
				"La operación no se puede completar porque entra en conflicto con los datos existentes.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception exception) {
		logger.error("Error inesperado al procesar una solicitud REST", exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrió un error interno al procesar la solicitud.");
	}

	private ResponseEntity<Map<String, Object>> validationError(Map<String, String> fieldErrors) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.put("message", "La solicitud contiene datos no válidos.");
		body.put("detail", "La solicitud contiene datos no válidos.");
		body.put("fieldErrors", fieldErrors);
		return ResponseEntity.badRequest().body(body);
	}

	private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(
				"status", status.value(),
				"error", status.getReasonPhrase(),
				"message", message,
				"detail", message));
	}
}
