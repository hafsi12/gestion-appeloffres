package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Livrable;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import com.terragis.appeloffre.terragis_project.entity.StatutValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivrableRepository extends JpaRepository<Livrable, Long> {

    List<Livrable> findByContratId(Long contratId);

    List<Livrable> findByStatutPaiement(StatutPaiement statutPaiement);

    List<Livrable> findByStatutValidation(StatutValidation statutValidation);

    @Query("SELECT l FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutPaiement = ?2")
    List<Livrable> findByContratIdAndStatutPaiement(Long contratId, StatutPaiement statutPaiement);

    @Query("SELECT l FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutValidation = ?2")
    List<Livrable> findByContratIdAndStatutValidation(Long contratId, StatutValidation statutValidation);

    @Query("SELECT SUM(l.montant) FROM Livrable l WHERE l.contrat.id = ?1")
    Double getTotalMontantByContrat(Long contratId);

    @Query("SELECT SUM(l.montant) FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutPaiement = 'PAYE'")
    Double getMontantPayeByContrat(Long contratId);

    @Query("SELECT COUNT(l) FROM Livrable l WHERE l.contrat.id = ?1")
    Long countByContratId(Long contratId);

    @Query("SELECT SUM(l.montant) FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutPaiement = 'NON_PAYE'")
    Double getMontantNonPayeByContrat(Long contratId);

    @Query("SELECT SUM(l.montant) FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutValidation = 'VALIDE'")
    Double getMontantValideByContrat(Long contratId);

    @Query("SELECT l FROM Livrable l WHERE l.contrat.id = ?1 AND l.statutPaiement = 'NON_PAYE' AND l.statutValidation = 'VALIDE'")
    List<Livrable> findLivrablesValidesNonPayesByContrat(Long contratId);

    void deleteByContratId(Long contratId);
}
