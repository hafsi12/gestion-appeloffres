package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chemin;  // URL ou chemin du fichier

    @ManyToOne
    @JoinColumn(name = "facture_id")
    private Facture facture;
}
