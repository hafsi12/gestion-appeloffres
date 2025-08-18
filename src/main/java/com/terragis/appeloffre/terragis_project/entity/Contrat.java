package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contrat")
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date startDate;
    private Date endDate;
    private String details;
    private String nameClient;
    private String statut; // ACTIF, TERMINE, SUSPENDU

    // Champs pour la signature électronique
    @Column(columnDefinition = "TEXT")
    private String signature; // Base64 de la signature
    private String signerName;
    private Date dateSignature;
    private boolean signed = false;

    // Champs pour le suivi
    private Date dateCreation;
    private Date dateEnvoi;

    @OneToOne
    @JoinColumn(name = "offre_id")
    @JsonManagedReference
    // Removed @JsonIgnore to allow serialization of the offer data
    private Offre offre;

    @Transient
    private Long offreId; // Pour recevoir l'ID depuis le frontend

    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private java.util.List<Livrable> livrables;
}
