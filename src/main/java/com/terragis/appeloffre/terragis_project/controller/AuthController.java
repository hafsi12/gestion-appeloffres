package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.dto.AuthResponse;
import com.terragis.appeloffre.terragis_project.dto.LoginRequest;
import com.terragis.appeloffre.terragis_project.dto.RegisterRequest;
import com.terragis.appeloffre.terragis_project.dto.UserResponse;
import com.terragis.appeloffre.terragis_project.entity.Role;
import com.terragis.appeloffre.terragis_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());
        try {
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            log.info("Connexion réussie pour l'utilisateur: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Erreur d'authentification pour l'utilisateur {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Erreur d'authentification: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        log.info("Tentative d'inscription pour l'utilisateur: {}", signUpRequest.getUsername());
        try {
            UserResponse userResponse = authService.registerUser(signUpRequest);
            log.info("Inscription réussie pour l'utilisateur: {}", signUpRequest.getUsername());
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            log.error("Erreur d'inscription pour l'utilisateur {}: {}", signUpRequest.getUsername(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = authService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        UserResponse user = authService.updateUserStatus(id, enabled);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Récupération de la liste des rôles");
        return ResponseEntity.ok(Arrays.asList(Role.values()));
    }
}
