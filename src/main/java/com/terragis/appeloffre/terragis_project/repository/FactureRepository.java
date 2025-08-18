package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Facture;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    // Trouver les factures par contrat
    @Query("SELECT f FROM Facture f WHERE f.contrat.id = :contratId")
    List<Facture> findByContratId(@Param("contratId") Long contratId);

    // Trouver les factures par statut de paiement
    @Query("SELECT f FROM Facture f WHERE f.statutFacture = :statut")
    List<Facture> findByStatutPaiement(@Param("statut") StatutPaiement statut);

    // Trouver les factures par numéro
    Optional<Facture> findByNumeroFacture(String numeroFacture);

    // Trouver les factures par période
    @Query("SELECT f FROM Facture f WHERE f.dateFacture BETWEEN :startDate AND :endDate")
    List<Facture> findByDateFactureBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Trouver les factures par client
    @Query("SELECT f FROM Facture f WHERE f.contrat.nameClient LIKE %:clientName%")
    List<Facture> findByClientName(@Param("clientName") String clientName);

    // Calculer le montant total des factures payées
    @Query("SELECT SUM(f.montantTotal) FROM Facture f WHERE f.statutFacture = 'PAYE'")
    Double getTotalMontantPaye();

    // Calculer le montant total des factures non payées
    @Query("SELECT SUM(f.montantTotal) FROM Facture f WHERE f.statutFacture = 'NON_PAYE'")
    Double getTotalMontantNonPaye();

    // Trouver les factures en retard (non payées depuis plus de 30 jours)
    @Query("SELECT f FROM Facture f WHERE f.statutFacture = 'NON_PAYE' AND f.dateFacture < :dateLimit")
    List<Facture> findFacturesEnRetard(@Param("dateLimit") Date dateLimit);

    // Statistiques par mois
    @Query("SELECT YEAR(f.dateFacture), MONTH(f.dateFacture), COUNT(f), SUM(f.montantTotal) " +
            "FROM Facture f GROUP BY YEAR(f.dateFacture), MONTH(f.dateFacture) " +
            "ORDER BY YEAR(f.dateFacture) DESC, MONTH(f.dateFacture) DESC")
    List<Object[]> getStatistiquesMensuelles();
}
