package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOpp;

    private String projectName;
    private String budget;
    private Date deadline;
    private String description;
    private boolean GO;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private MaitreOeuvrage client;

    @OneToOne
    @JoinColumn(name = "etat_id")
    private EtatOpportunite etat;

    @OneToMany(mappedBy = "opportunite", cascade = CascadeType.ALL)
    private List<DocumentOpportunite> documents;

    @OneToOne(mappedBy = "opportunite", cascade = CascadeType.ALL)
    private Offre offre;
}
