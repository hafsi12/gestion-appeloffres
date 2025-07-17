package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOpportunite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String fileType;
    private String path;

    @ManyToOne
    @JoinColumn(name = "opportunite_id")
    private Opportunite opportunite;
}
