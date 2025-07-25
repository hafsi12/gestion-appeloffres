package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Offre;
import com.terragis.appeloffre.terragis_project.entity.Adjuge;
import com.terragis.appeloffre.terragis_project.entity.Opportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    Optional<Offre> findByIdOffre(Long id);

    @Query("SELECT o FROM Offre o WHERE o.opportunite.idOpp = ?1")
    List<Offre> findByOpportuniteId(Long opportuniteId);

    @Query("SELECT o FROM Offre o WHERE o.adjuge = 'GAGNEE' AND o.contrat IS NULL")
    List<Offre> findOffresGagneesSansContrat();

    Optional<Offre> findByOpportunite(Opportunite opportunite);
}
