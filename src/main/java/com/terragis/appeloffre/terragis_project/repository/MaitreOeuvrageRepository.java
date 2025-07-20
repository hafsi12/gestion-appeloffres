package com.terragis.appeloffre.terragis_project.repository;

import com.terragis.appeloffre.terragis_project.entity.MaitreOeuvrage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaitreOeuvrageRepository extends JpaRepository<MaitreOeuvrage, Long> {
    List<MaitreOeuvrage> findByArchived(boolean archived);
}
