package com.terragis.appeloffre.terragis_project.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateEvenement;
    @ManyToMany(mappedBy = "events") // Mappé par le champ 'events' dans l'entité Offre
    @JsonIgnore // Empêche la référence circulaire lors de la sérialisation JSON
    private List<Offre> offres; // Liste des offres associées à cet événement
}
