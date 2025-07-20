package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import this
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
    @JsonIgnore // Add this annotation to break the circular reference
    private Opportunite opportunite;
}
