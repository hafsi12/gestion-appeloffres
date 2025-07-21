package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import this
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOffre;
    private double budget;
    private String detail;
    private boolean sent;

    @Enumerated(EnumType.STRING)
    private Adjuge adjuge;

    @OneToOne(mappedBy = "offre") // Inverse side, mapped by the 'offre' field in Opportunite
    @JsonIgnore // Prevents circular reference
    private Opportunite opportunite;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    private List<DocumentOffre> documents;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    private List<Tache> taches;

    @OneToOne(mappedBy = "offre", cascade = CascadeType.ALL)
    private Contrat contrat;

    @ManyToMany
    @JoinTable(
            name = "offre_event",
            joinColumns = @JoinColumn(name = "offre_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events;
}
