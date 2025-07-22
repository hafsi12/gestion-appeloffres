package com.terragis.appeloffre.terragis_project.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String detail;
    private LocalDate deadline;
    private String assignedPerson;
    private boolean checked  ;
    @ManyToOne
    @JoinColumn(name = "offre_id")
    @JsonIgnore
    private Offre offre;
}
