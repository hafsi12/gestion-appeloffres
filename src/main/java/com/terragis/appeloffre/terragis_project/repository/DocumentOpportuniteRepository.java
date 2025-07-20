package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.DocumentOpportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentOpportuniteRepository extends JpaRepository<DocumentOpportunite, Long> {

    @Modifying
    @Query("DELETE FROM DocumentOpportunite d WHERE d.opportunite.idOpp = :opportuniteId")
    void deleteByOpportuniteId(@Param("opportuniteId") Long opportuniteId);
}