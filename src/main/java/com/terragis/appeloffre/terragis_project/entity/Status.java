package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String libelle; // Par exemple : "Validé", "Refusé", "En attente", etc.
}
