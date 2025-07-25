package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    @Query("SELECT t FROM Tache t WHERE t.offre.idOffre = ?1")
    List<Tache> findByOffreId(Long offreId);

    @Query("SELECT t FROM Tache t WHERE t.checked = ?1")
    List<Tache> findByChecked(boolean checked);
}
