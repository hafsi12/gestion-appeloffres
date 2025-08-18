package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    @Query("SELECT DISTINCT c FROM Contrat c LEFT JOIN FETCH c.livrables")
    List<Contrat> findAllWithLivrables();

    @Query("SELECT c FROM Contrat c LEFT JOIN FETCH c.livrables WHERE c.id = :id")
    Optional<Contrat> findByIdWithLivrables(Long id);

    @Query("SELECT c FROM Contrat c WHERE c.statut = 'ACTIF'")
    List<Contrat> findActiveContrats();

    @Query("SELECT DISTINCT c FROM Contrat c LEFT JOIN FETCH c.livrables WHERE c.statut = 'ACTIF'")
    List<Contrat> findActiveContratsWithLivrables();

    @Query("SELECT c FROM Contrat c WHERE c.signed = true")
    List<Contrat> findSignedContrats();

    @Query("SELECT DISTINCT c FROM Contrat c LEFT JOIN FETCH c.livrables WHERE c.signed = true")
    List<Contrat> findSignedContratsWithLivrables();

    @Query("SELECT c FROM Contrat c WHERE c.nameClient LIKE %?1%")
    List<Contrat> findByClientNameContaining(String clientName);

    @Query("SELECT DISTINCT c FROM Contrat c LEFT JOIN FETCH c.livrables WHERE c.nameClient LIKE %?1%")
    List<Contrat> findByClientNameContainingWithLivrables(String clientName);

    @Query("SELECT DISTINCT c FROM Contrat c LEFT JOIN FETCH c.livrables l WHERE c.id NOT IN (SELECT DISTINCT f.contrat.id FROM Facture f WHERE f.contrat IS NOT NULL)")
    List<Contrat> findContratsWithoutFacture();
}
