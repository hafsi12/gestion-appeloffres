package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;
import com.terragis.appeloffre.terragis_project.entity.Adjuge;

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

    @OneToOne
    @JoinColumn(name = "opportunite_id")
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
