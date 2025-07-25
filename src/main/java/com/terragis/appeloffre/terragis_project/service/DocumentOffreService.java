package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.DocumentOffre;
import com.terragis.appeloffre.terragis_project.repository.DocumentOffreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentOffreService {
    
    private final DocumentOffreRepository documentOffreRepository;
    
    public DocumentOffre getDocumentById(Long id) {
        return documentOffreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + id));
    }
}
