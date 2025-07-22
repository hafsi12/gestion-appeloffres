package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import this if needed for other relationships
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

    // Offre is now the owning side of the OneToOne relationship with Opportunite
    @OneToOne
    @JoinColumn(name = "opportunite_id") // This will create the foreign key in the 'offre' table
    private Opportunite opportunite;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentOffre> documents;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
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

    @Transient
    private Long incomingOpportuniteId;
}
