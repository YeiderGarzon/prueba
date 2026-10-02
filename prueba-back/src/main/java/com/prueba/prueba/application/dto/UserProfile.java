package com.prueba.prueba.application.dto;

import com.prueba.prueba.domain.model.UserRole;

public record UserProfile(String username, String fullName, UserRole role) {
}
