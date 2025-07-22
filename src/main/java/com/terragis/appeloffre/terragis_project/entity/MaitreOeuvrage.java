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
public class MaitreOeuvrage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;
    private String name;
    private String webSite;
    private String address;
    private boolean archived;
    private String secteur;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts;
    @OneToMany(mappedBy = "client")
    @JsonIgnore // Add this annotation to break the circular reference
    private List<Opportunite> opportunites;
}
