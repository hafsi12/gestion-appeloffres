package com.terragis.appeloffre.terragis_project.entity;

public enum Role {
    ADMIN("Administrateur - Accès complet"),
    GESTION_CLIENTS_OPPORTUNITES("Gestion des clients et opportunités"),
    GESTION_OFFRES("Gestion des offres"),
    GESTION_CONTRATS("Gestion des contrats");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
