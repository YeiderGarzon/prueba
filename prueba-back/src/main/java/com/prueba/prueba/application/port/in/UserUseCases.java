package com.prueba.prueba.application.port.in;

import java.util.List;

import com.prueba.prueba.application.dto.UserRequest;
import com.prueba.prueba.application.dto.UserResponse;
import com.prueba.prueba.application.dto.UserProfileUpdateRequest;
import com.prueba.prueba.application.dto.UserUpdateRequest;

public interface UserUseCases {
	List<UserResponse> findAll();

	UserResponse findById(Long id);

	UserResponse findCurrent(String username);

	UserResponse updateCurrent(String username, UserProfileUpdateRequest request);

	UserResponse create(UserRequest request);

	UserResponse update(Long id, UserUpdateRequest request);

	void delete(Long id, String currentUsername);
}
