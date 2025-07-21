package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.*;
import com.terragis.appeloffre.terragis_project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpportuniteService {
    private final OpportuniteRepository opportuniteRepository;
    private final DocumentOpportuniteRepository documentRepository;
    private final EtatOpportuniteRepository etatRepository;
    private final MaitreOeuvrageRepository clientRepository;
    private final FileStorageService fileStorageService;

    public OpportuniteService(OpportuniteRepository opportuniteRepository,
                              DocumentOpportuniteRepository documentRepository,
                              EtatOpportuniteRepository etatRepository,
                              MaitreOeuvrageRepository clientRepository,
                              FileStorageService fileStorageService) {
        this.opportuniteRepository = opportuniteRepository;
        this.documentRepository = documentRepository;
        this.etatRepository = etatRepository;
        this.clientRepository = clientRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Opportunite> getAllOpportunites() {
        return opportuniteRepository.findAll();
    }

    public Optional<Opportunite> getOpportuniteById(Long id) {
        return opportuniteRepository.findById(id);
    }

    public Opportunite createOpportunite(Opportunite opportunite, List<MultipartFile> files) {
        System.out.println("Creating opportunity with client ID: " + opportunite.getIncomingClientId());
        if (opportunite.getIncomingClientId() != null) {
            MaitreOeuvrage client = clientRepository.findById(opportunite.getIncomingClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + opportunite.getIncomingClientId()));
            opportunite.setClient(client);
        }
        if (opportunite.getEtat() == null) {
            opportunite.setEtat(new EtatOpportunite());
            opportunite.getEtat().setStatut(EtatOpportuniteEnum.EN_COURS);
        }
        opportunite.getEtat().setOpportunite(opportunite);
        etatRepository.save(opportunite.getEtat());

        if (opportunite.getDocuments() != null && !opportunite.getDocuments().isEmpty()) {
            List<DocumentOpportunite> processedDocuments = opportunite.getDocuments().stream()
                    .map(doc -> {
                        if (doc.getPath() != null && !doc.getPath().isEmpty()) {
                            Optional<MultipartFile> matchingFile = files != null ? files.stream()
                                    .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(doc.getPath()))
                                    .findFirst() : Optional.empty();
                            if (matchingFile.isPresent()) {
                                String storedFileName = fileStorageService.storeFile(matchingFile.get());
                                doc.setPath(storedFileName);
                            }
                        }
                        doc.setOpportunite(opportunite);
                        return doc;
                    })
                    .collect(Collectors.toList());
            opportunite.setDocuments(processedDocuments);
        }
        Opportunite savedOpportunite = opportuniteRepository.save(opportunite);
        return savedOpportunite;
    }

    public Opportunite updateOpportunite(Long id, Opportunite opportuniteDetails, List<MultipartFile> files) {
        System.out.println("OpportuniteService: Attempting to update opportunity with ID: " + id);
        Opportunite existingOpportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> {
                    System.err.println("OpportuniteService: Opportunité non trouvée pour l'ID: " + id);
                    return new RuntimeException("Opportunité non trouvée");
                });
        System.out.println("OpportuniteService: Found existing opportunity with ID: " + existingOpportunite.getIdOpp() + ", Project Name: " + existingOpportunite.getProjectName());

        // Update scalar fields
        existingOpportunite.setProjectName(opportuniteDetails.getProjectName());
        existingOpportunite.setBudget(opportuniteDetails.getBudget());
        existingOpportunite.setDeadline(opportuniteDetails.getDeadline());
        existingOpportunite.setDescription(opportuniteDetails.getDescription());

        // Update client
        if (opportuniteDetails.getIncomingClientId() != null) {
            MaitreOeuvrage client = clientRepository.findById(opportuniteDetails.getIncomingClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + opportuniteDetails.getIncomingClientId()));
            existingOpportunite.setClient(client);
        } else {
            existingOpportunite.setClient(null);
        }

        // Update EtatOpportunite
        if (opportuniteDetails.getEtat() != null) {
            EtatOpportunite etat = existingOpportunite.getEtat();
            if (etat == null) {
                etat = new EtatOpportunite();
                etat.setOpportunite(existingOpportunite);
            }
            etat.setStatut(opportuniteDetails.getEtat().getStatut());
            etat.setJustification(opportuniteDetails.getEtat().getJustification());
            etatRepository.save(etat);
            existingOpportunite.setEtat(etat);
        }

        // --- Handle documents collection synchronization ---
        List<DocumentOpportunite> incomingDocuments = opportuniteDetails.getDocuments() != null ?
                opportuniteDetails.getDocuments() : new java.util.ArrayList<>();

        // 1. Identify documents to remove (those in existingOpportunite but not in incomingDocuments)
        List<DocumentOpportunite> documentsToRemove = existingOpportunite.getDocuments().stream()
                .filter(existingDoc -> incomingDocuments.stream().noneMatch(incomingDoc -> existingDoc.getId() != null && existingDoc.getId().equals(incomingDoc.getId())))
                .collect(Collectors.toList());

        // 2. Remove identified documents from the existing collection.
        //    Because of orphanRemoval=true, this will trigger deletion from DB.
        //    Also delete the actual files.
        documentsToRemove.forEach(doc -> {
            existingOpportunite.getDocuments().remove(doc);
            if (doc.getPath() != null && !doc.getPath().isEmpty()) {
                fileStorageService.deleteFile(doc.getPath());
            }
        });

        // 3. Iterate through incoming documents to add new ones or update existing ones
        for (DocumentOpportunite incomingDoc : incomingDocuments) {
            if (incomingDoc.getId() == null) {
                // This is a new document
                DocumentOpportunite newDoc = new DocumentOpportunite();
                newDoc.setTitle(incomingDoc.getTitle());
                newDoc.setDescription(incomingDoc.getDescription());
                newDoc.setFileType(incomingDoc.getFileType());

                // Handle file storage for new documents
                if (incomingDoc.getPath() != null && !incomingDoc.getPath().isEmpty()) { // incomingDoc.path here is the original filename
                    Optional<MultipartFile> matchingFile = files != null ? files.stream()
                            .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(incomingDoc.getPath()))
                            .findFirst() : Optional.empty();
                    if (matchingFile.isPresent()) {
                        String storedFileName = fileStorageService.storeFile(matchingFile.get());
                        newDoc.setPath(storedFileName);
                    } else {
                        System.err.println("Warning: New document metadata received without corresponding file: " + incomingDoc.getTitle());
                        newDoc.setPath(null);
                    }
                }
                newDoc.setOpportunite(existingOpportunite);
                existingOpportunite.getDocuments().add(newDoc);
            } else {
                // This is an existing document, find it in the existing collection and update
                existingOpportunite.getDocuments().stream()
                        .filter(existingDoc -> existingDoc.getId().equals(incomingDoc.getId()))
                        .findFirst()
                        .ifPresent(existingDoc -> {
                            existingDoc.setTitle(incomingDoc.getTitle());
                            existingDoc.setDescription(incomingDoc.getDescription());
                            existingDoc.setFileType(incomingDoc.getFileType());

                            // Check if a new file was uploaded for this existing document
                            // incomingDoc.path will be the original filename if a new file was selected.
                            if (incomingDoc.getPath() != null && !incomingDoc.getPath().isEmpty()) {
                                Optional<MultipartFile> matchingFile = files != null ? files.stream()
                                        .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(incomingDoc.getPath()))
                                        .findFirst() : Optional.empty();
                                if (matchingFile.isPresent()) {
                                    // A new file was provided for an existing document, store it and update path
                                    String storedFileName = fileStorageService.storeFile(matchingFile.get());
                                    // Optionally delete the old file if path changed
                                    if (existingDoc.getPath() != null && !existingDoc.getPath().equals(storedFileName)) {
                                        fileStorageService.deleteFile(existingDoc.getPath());
                                    }
                                    existingDoc.setPath(storedFileName);
                                }
                                // If incomingDoc.path is not empty but no matching file, it means it's an existing document
                                // whose file was not changed, so its path should remain the stored path.
                                // No action needed here, as existingDoc.path already holds the stored path.
                            }
                        });
            }
        }

        return opportuniteRepository.save(existingOpportunite);
    }

    public void deleteOpportunite(Long id) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));
        if (opportunite.getDocuments() != null) {
            opportunite.getDocuments().forEach(doc -> {
                fileStorageService.deleteFile(doc.getPath());
            });
        }
        opportuniteRepository.delete(opportunite);
    }

    public Opportunite updateOpportuniteStatus(Long id, EtatOpportuniteEnum statut, String justification) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));
        EtatOpportunite etat = opportunite.getEtat();
        if (etat == null) {
            etat = new EtatOpportunite();
            etat.setOpportunite(opportunite);
        }
        etat.setStatut(statut);
        etat.setJustification(statut == EtatOpportuniteEnum.NO_GO ? justification : null);
        opportunite.setEtat(etatRepository.save(etat));
        return opportuniteRepository.save(opportunite);
    }
}
