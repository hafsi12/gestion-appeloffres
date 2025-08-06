package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DiagnosticController {

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/status")
    public ResponseEntity<?> getServerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Serveur Spring Boot opérationnel");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Récupérer le token depuis l'en-tête
            String headerAuth = request.getHeader("Authorization");
            String token = null;

            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                token = headerAuth.substring(7);
                response.put("tokenPresent", true);
                response.put("tokenPrefix", token.substring(0, Math.min(20, token.length())) + "...");

                // Valider le token
                if (jwtUtils.validateJwtToken(token)) {
                    response.put("tokenValid", true);

                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    response.put("username", username);

                    // Récupérer l'authentification actuelle
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null) {
                        response.put("authenticated", auth.isAuthenticated());
                        response.put("authorities", auth.getAuthorities());
                        response.put("principal", auth.getName());
                    } else {
                        response.put("authenticated", false);
                        response.put("error", "Aucune authentification dans le contexte de sécurité");
                    }
                } else {
                    response.put("tokenValid", false);
                    response.put("error", "Token JWT invalide");
                }
            } else {
                response.put("tokenPresent", false);
                response.put("error", "Aucun token Bearer trouvé dans l'en-tête Authorization");
            }

            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Erreur lors du diagnostic: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        Map<String, Object> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            response.put("message", "Utilisateur authentifié avec succès");
        } else {
            response.put("authenticated", false);
            response.put("message", "Utilisateur non authentifié");
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
