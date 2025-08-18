package com.terragis.appeloffre.terragis_project.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livrable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String description;
    private Date dateLivraison;
    @Enumerated(EnumType.STRING)
    private StatutValidation statutValidation;
    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement;
    private Double montant;
    private String fichierJoint; // chemin ou URL du fichier joint

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    @JsonBackReference
    private Contrat contrat;
}
