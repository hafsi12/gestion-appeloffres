package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.DocumentOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentOffreRepository extends JpaRepository<DocumentOffre, Long> {

    @Query("SELECT d FROM DocumentOffre d WHERE d.offre.idOffre = ?1")
    List<DocumentOffre> findByOffreId(Long offreId);

    @Query("SELECT d FROM DocumentOffre d WHERE d.type = ?1")
    List<DocumentOffre> findByType(String type);
}
