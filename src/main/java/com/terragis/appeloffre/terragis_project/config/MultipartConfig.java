package com.terragis.appeloffre.terragis_project.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Taille maximale par fichier : 50MB
        factory.setMaxFileSize(DataSize.ofMegabytes(50));

        // Taille maximale de la requête : 100MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));

        // Seuil à partir duquel les fichiers sont écrits sur disque
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));

        return factory.createMultipartConfig();
    }
}
