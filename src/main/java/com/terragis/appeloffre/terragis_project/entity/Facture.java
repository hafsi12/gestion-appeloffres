package com.terragis.appeloffre.terragis_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroFacture;

    private Date dateFacture;

    private Double montantTotal;

    // Relation avec le contrat
    @ManyToOne
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    // Statut de la facture (PAYE, NON_PAYE)
    @Enumerated(EnumType.STRING)
    private StatutPaiement statutFacture;

    // One facture a plusieurs fichiers
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> fichiers;

    // One facture a un seul status
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status statut;

    // Méthode pour calculer le statut de la facture basé sur les livrables
    @Transient
    public StatutPaiement calculateStatutFacture() {
        if (contrat != null && contrat.getLivrables() != null && !contrat.getLivrables().isEmpty()) {
            boolean allPaid = contrat.getLivrables().stream()
                    .allMatch(livrable ->
                            livrable.getStatutPaiement() == StatutPaiement.PAYE ||
                                    livrable.getStatutPaiement() == StatutPaiement.SOLDE
                    );
            return allPaid ? StatutPaiement.PAYE : StatutPaiement.NON_PAYE;
        }
        return StatutPaiement.NON_PAYE;
    }

    // Méthode pour calculer le montant total basé sur les livrables
    @Transient
    public Double calculateMontantTotal() {
        if (contrat != null && contrat.getLivrables() != null && !contrat.getLivrables().isEmpty()) {
            return contrat.getLivrables().stream()
                    .filter(livrable -> livrable.getMontant() != null)
                    .mapToDouble(Livrable::getMontant)
                    .sum();
        }
        return 0.0;
    }
}
