package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    @Query("SELECT c FROM Contrat c WHERE c.statut = 'ACTIF'")
    List<Contrat> findActiveContrats();

    @Query("SELECT c FROM Contrat c WHERE c.signed = true")
    List<Contrat> findSignedContrats();

    @Query("SELECT c FROM Contrat c WHERE c.nameClient LIKE %?1%")
    List<Contrat> findByClientNameContaining(String clientName);
}
