package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String namefile;
    private String cheminFichier; // chemin dans le système ou nom dans le stockage
    private String type; // ex : PDF, DOCX...
    private String description;
    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;
}
