package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date endDate;
    private Date startdDate;
    private String details ;
    private String nameClient;

    @OneToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;

    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    private java.util.List<Livrable> livrables;

}
