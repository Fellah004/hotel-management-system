package com.hms.authservice.service;

import com.hms.authservice.dto.request.CreateUserRequest;
import com.hms.authservice.dto.request.UpdateUserRequest;
import com.hms.authservice.dto.response.UserResponse;
import com.hms.authservice.entity.Role;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(Role role);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    UserResponse unlockUser(Long id);
    UserResponse setUserActiveStatus(Long id, boolean active);
}
