package com.terragis.appeloffre.terragis_project.config;

import com.terragis.appeloffre.terragis_project.entity.Role;
import com.terragis.appeloffre.terragis_project.entity.User;
import com.terragis.appeloffre.terragis_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("🚀 Initialisation des données utilisateur...");

            // Créer l'utilisateur admin avec accès complet
            recreateUserIfNeeded("admin", "Admin", "System", "admin@terragis.com",
                    "admin123", Role.ADMIN);

            // Créer user1 avec gestion clients et opportunités
            recreateUserIfNeeded("user1", "Utilisateur", "Un", "user1@terragis.com",
                    "password123", Role.GESTION_CLIENTS_OPPORTUNITES);

            // Créer user2 avec gestion des offres uniquement
            recreateUserIfNeeded("user2", "Utilisateur", "Deux", "user2@terragis.com",
                    "password123", Role.GESTION_OFFRES);

            // Créer user3 avec gestion des contrats uniquement
            recreateUserIfNeeded("user3", "Utilisateur", "Trois", "user3@terragis.com",
                    "password123", Role.GESTION_CONTRATS);

            log.info("✅ Initialisation des données terminée avec succès!");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation des données: {}", e.getMessage());
            log.error("💡 Vérifiez que la contrainte de rôle dans la base de données inclut 'ADMIN'");
            log.error("💡 Exécutez le script SQL: scripts/update-role-constraint.sql");
            throw e;
        }
    }

    private void recreateUserIfNeeded(String username, String firstName, String lastName,
                                      String email, String password, Role role) {
        try {
            // Supprimer l'utilisateur existant s'il existe
            userRepository.findByUsername(username).ifPresent(user -> {
                log.info("🗑️ Suppression de l'utilisateur existant: {}", username);
                userRepository.delete(user);
            });

            // Créer le nouvel utilisateur
            User user = User.builder()
                    .username(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);
            log.info("✅ Utilisateur {} créé avec succès - Rôle: {}", username, role.getDescription());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'utilisateur {}: {}", username, e.getMessage());
            if (e.getMessage().contains("users_role_check")) {
                log.error("💡 La contrainte de rôle dans la base de données doit être mise à jour pour inclure: {}", role);
                log.error("💡 Exécutez: ALTER TABLE users DROP CONSTRAINT users_role_check;");
                log.error("💡 Puis: ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'GESTION_CLIENTS_OPPORTUNITES', 'GESTION_OFFRES', 'GESTION_CONTRATS'));");
            }
            throw e;
        }
    }
}
