package com.prueba.prueba.application.exception;

public class ApplicationException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7251621629646938582L;

	public enum Kind {
		BAD_REQUEST,
		UNAUTHORIZED,
		NOT_FOUND,
		CONFLICT
	}

	private final Kind kind;

	public ApplicationException(Kind kind, String message) {
		super(message);
		this.kind = kind;
	}

	public Kind getKind() {
		return kind;
	}
}
