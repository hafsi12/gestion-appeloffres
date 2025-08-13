package com.terragis.appeloffre.terragis_project.config;

import com.terragis.appeloffre.terragis_project.security.AuthEntryPointJwt;
import com.terragis.appeloffre.terragis_project.security.AuthTokenFilter;
import com.terragis.appeloffre.terragis_project.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // -------------------------
                                // Endpoints publics
                                // -------------------------
                                .requestMatchers("/api/auth/signin").permitAll()
                                .requestMatchers("/api/auth/signup").permitAll()
                                .requestMatchers("/api/auth/roles").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/api/diagnostic/**").permitAll()
                                .requestMatchers("/error").permitAll()

                                // -------------------------
                                // Routes exactes accessibles à tous les utilisateurs authentifiés
                                // (listes racines)
                                // -------------------------
                                .requestMatchers("/api/clients").hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")
                                .requestMatchers("/api/opportunites").hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")
                                .requestMatchers("/api/offres").hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")
                                .requestMatchers("/api/contrats").hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")

                                // -------------------------
                                // Clients: autoriser la lecture (GET) pour les rôles de consultation (avant la règle générale)
                                // -------------------------
                                .requestMatchers(HttpMethod.GET, "/api/clients/**")
                                .hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")

                                // Autoriser lecture spécifique des contacts d'un client
                                .requestMatchers(HttpMethod.GET, "/api/clients/*/contacts")
                                .hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")

                                // Gestion complète des clients (CRUD) - réservé aux gestionnaires clients et admin
                                .requestMatchers("/api/clients/**")
                                .hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES")

                                // -------------------------
                                // Opportunités
                                // - accès à go-disponibles pour les rôles concernés
                                // - autres opérations opportunités (CRUD) pour gestion_clients_opportunites
                                // -------------------------
                                .requestMatchers("/api/opportunites/go-disponibles")
                                .hasAnyRole("ADMIN", "GESTION_OFFRES", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_CONTRATS")
                                // Opportunités archivées - lecture autorisée à certains rôles
                                .requestMatchers(HttpMethod.GET, "/api/opportunites/archived")
                                .hasAnyRole("ADMIN", "GESTION_OFFRES", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_CONTRATS", "USER2", "USER3")

                                .requestMatchers("/api/opportunites/**")
                                .hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES")

                                // -------------------------
                                // Offres
                                // - route spéciale pour offres gagnées visible par plusieurs rôles
                                // - gestion complète des offres réservée aux gestion_offres/admin
                                // -------------------------
                                .requestMatchers("/api/offres/gagnees-sans-contrat")
                                .hasAnyRole("ADMIN", "GESTION_OFFRES", "GESTION_CONTRATS", "GESTION_CLIENTS_OPPORTUNITES")

                                .requestMatchers("/api/offres/**")
                                .hasAnyRole("ADMIN", "GESTION_OFFRES")

                                // -------------------------
                                // Contrats : règles fines (ordre important)
                                // - GET (lecture) autorisé pour les rôles de lecture/gestion
                                // - POST /api/contrats/{id}/generate-pdf autorisé pour rôles de lecture/gestion
                                // - Règle générale pour opérations de gestion réservée aux gestion_contrats/admin
                                // -------------------------
                                .requestMatchers(HttpMethod.GET, "/api/contrats/**")
                                .hasAnyRole("ADMIN", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES", "GESTION_CONTRATS")

                                // Pattern valide : single segment for id then generate-pdf
                                .requestMatchers(HttpMethod.POST, "/api/contrats/*/generate-pdf")
                                .hasAnyRole("ADMIN", "GESTION_CONTRATS", "GESTION_CLIENTS_OPPORTUNITES", "GESTION_OFFRES")

                                // Règle générale pour modification/creation/suppression des contrats (PUT/POST autres/DELETE)
                                .requestMatchers("/api/contrats/**")
                                .hasAnyRole("ADMIN", "GESTION_CONTRATS")

                                // -------------------------
                                // Tout le reste nécessite une authentification
                                // -------------------------
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
