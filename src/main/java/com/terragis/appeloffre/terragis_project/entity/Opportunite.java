package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // Import this
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opportunite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOpp;
    private String projectName;
    private String budget;
    private Date deadline;
    private String description;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private MaitreOeuvrage client;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "etat_id")
    private EtatOpportunite etat;

    @OneToMany(mappedBy = "opportunite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentOpportunite> documents;

    // Opportunite is now the inverse side of the OneToOne relationship with Offre
    @OneToOne(mappedBy = "opportunite") // Mapped by the 'opportunite' field in Offre
    @JsonBackReference // Prevents circular reference during JSON serialization
    private Offre offre;

    @Transient
    private Long incomingClientId;

    @JsonProperty("client")
    public void setClientFromRequest(Object clientObject) {
        if (clientObject instanceof Map) {
            Map<?, ?> clientMap = (Map<?, ?>) clientObject;
            if (clientMap.containsKey("idClient")) {
                this.incomingClientId = ((Number) clientMap.get("idClient")).longValue();
            }
        }
        this.client = null;
    }
}
