package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Opportunite;
import com.terragis.appeloffre.terragis_project.entity.Offre;
import com.terragis.appeloffre.terragis_project.entity.EtatOpportuniteEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {

    // Trouver par offre
    Optional<Opportunite> findByOffre(Offre offre);

    // Find non-archived opportunities
    List<Opportunite> findByArchivedFalse();

    // Find archived opportunities
    List<Opportunite> findByArchivedTrue();



    @Query("SELECT o FROM Opportunite o WHERE o.etat.statut = com.terragis.appeloffre.terragis_project.entity.EtatOpportuniteEnum.GO AND o.offre IS NULL")
    List<Opportunite> findOpportunitesGoDisponibles();


}
