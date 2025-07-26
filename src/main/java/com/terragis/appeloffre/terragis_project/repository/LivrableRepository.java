package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Livrable;
import com.terragis.appeloffre.terragis_project.entity.StatutValidation;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivrableRepository extends JpaRepository<Livrable, Long> {
    
    List<Livrable> findByContratId(Long contratId);
    
    @Query("SELECT l FROM Livrable l WHERE l.statutValidation = ?1")
    List<Livrable> findByStatutValidation(StatutValidation statut);
    
    @Query("SELECT l FROM Livrable l WHERE l.statutPaiement = ?1")
    List<Livrable> findByStatutPaiement(StatutPaiement statut);

    void deleteByContratId(Long contratId);
}
