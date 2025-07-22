package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Opportunite;
import com.terragis.appeloffre.terragis_project.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {
    // Method to find an Opportunite by its associated Offre
    Optional<Opportunite> findByOffre(Offre offre);
}
