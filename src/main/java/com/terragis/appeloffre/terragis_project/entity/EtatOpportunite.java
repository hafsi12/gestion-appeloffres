package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtatOpportunite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EtatOpportuniteEnum statut;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @OneToOne(mappedBy = "etat")
    @JsonIgnore
    private Opportunite opportunite;
}
