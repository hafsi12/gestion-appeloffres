package com.terragis.appeloffre.terragis_project.entity;

public enum TypeDossier {
    TECHNIQUE("Dossier Technique"),
    ADMINISTRATIF("Dossier Administratif"), 
    FINANCIER("Dossier Financier");
    
    private final String displayName;
    
    TypeDossier(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
