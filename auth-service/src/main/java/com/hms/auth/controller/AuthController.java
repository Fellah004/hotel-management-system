package com.hms.auth.controller;

import com.hms.auth.entity.Role;
import com.hms.auth.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody User user) {

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password) {

        return ResponseEntity.ok(
                "Login request received for user: " + username
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam UUID userId) {

        return ResponseEntity.ok(
                "Logout request received for user: " + userId
        );
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(
            @Valid @RequestBody Role role) {

        return ResponseEntity.ok(role);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(List.of());
    }
}
