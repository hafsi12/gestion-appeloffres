package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.*;
import com.terragis.appeloffre.terragis_project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OffreService {
    private final OffreRepository offreRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final DocumentOffreRepository documentOffreRepository;
    private final TacheRepository tacheRepository;
    private final FileStorageService fileStorageService;

    public OffreService(OffreRepository offreRepository,
                        OpportuniteRepository opportuniteRepository,
                        DocumentOffreRepository documentOffreRepository,
                        TacheRepository tacheRepository,
                        FileStorageService fileStorageService) {
        this.offreRepository = offreRepository;
        this.opportuniteRepository = opportuniteRepository;
        this.documentOffreRepository = documentOffreRepository;
        this.tacheRepository = tacheRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Offre> getAllOffres() {
        return offreRepository.findAll();
    }

    public Optional<Offre> getOffreById(Long id) {
        return offreRepository.findById(id);
    }

    public Offre createOffre(Offre offre, List<MultipartFile> files) {
        Opportunite opportunite = null;
        if (offre.getIncomingOpportuniteId() != null) {
            opportunite = opportuniteRepository.findById(offre.getIncomingOpportuniteId())
                    .orElseThrow(() -> new RuntimeException("Opportunité non trouvée avec l'ID: " + offre.getIncomingOpportuniteId()));
            offre.setOpportunite(opportunite); // Set the Opportunite object on the Offre
        }

        if (offre.getAdjuge() == null) {
            offre.setAdjuge(Adjuge.EN_ATTENTE);
        }

        // Handle documents
        if (offre.getDocuments() != null && !offre.getDocuments().isEmpty()) {
            List<DocumentOffre> processedDocuments = new ArrayList<>();
            for (DocumentOffre doc : offre.getDocuments()) {
                if (doc.getCheminFichier() != null && !doc.getCheminFichier().isEmpty()) {
                    Optional<MultipartFile> matchingFile = files != null ? files.stream()
                            .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(doc.getCheminFichier()))
                            .findFirst() : Optional.empty();
                    if (matchingFile.isPresent()) {
                        String storedFileName = fileStorageService.storeFile(matchingFile.get());
                        doc.setCheminFichier(storedFileName);
                    }
                }
                doc.setOffre(offre);
                processedDocuments.add(doc);
            }
            offre.setDocuments(processedDocuments);
        }

        // Handle tasks
        if (offre.getTaches() != null) {
            offre.getTaches().forEach(tache -> tache.setOffre(offre));
        }

        Offre savedOffre = offreRepository.save(offre);

        // Maintain bidirectional consistency for Opportunite
        if (opportunite != null) {
            // If the opportunite was previously linked to another offre, break that link
            Opportunite oldOpportuniteLinkedToThisOffre = opportuniteRepository.findByOffre(savedOffre).orElse(null);
            if (oldOpportuniteLinkedToThisOffre != null && !oldOpportuniteLinkedToThisOffre.getIdOpp().equals(opportunite.getIdOpp())) {
                oldOpportuniteLinkedToThisOffre.setOffre(null);
                opportuniteRepository.save(oldOpportuniteLinkedToThisOffre);
            }
            opportunite.setOffre(savedOffre);
            opportuniteRepository.save(opportunite); // Save Opportunite to update its 'offre' field
        }

        return savedOffre;
    }

    public Offre updateOffre(Long id, Offre offreDetails, List<MultipartFile> files) {
        Offre existingOffre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));

        // Handle potential change in associated Opportunite
        Opportunite oldOpportunite = existingOffre.getOpportunite();
        Opportunite newOpportunite = null;

        if (offreDetails.getIncomingOpportuniteId() != null) {
            newOpportunite = opportuniteRepository.findById(offreDetails.getIncomingOpportuniteId())
                    .orElseThrow(() -> new RuntimeException("Nouvelle Opportunité non trouvée avec l'ID: " + offreDetails.getIncomingOpportuniteId()));
            existingOffre.setOpportunite(newOpportunite); // Set the new Opportunite on the Offre
        } else {
            existingOffre.setOpportunite(null); // Disassociate if incomingOpportuniteId is null
        }

        existingOffre.setBudget(offreDetails.getBudget());
        existingOffre.setDetail(offreDetails.getDetail());
        existingOffre.setSent(offreDetails.isSent());
        existingOffre.setAdjuge(offreDetails.getAdjuge());

        // --- Handle documents collection synchronization ---
        List<DocumentOffre> incomingDocuments = offreDetails.getDocuments() != null ?
                offreDetails.getDocuments() : new ArrayList<>();
        List<DocumentOffre> documentsToRemove = existingOffre.getDocuments().stream()
                .filter(existingDoc -> incomingDocuments.stream().noneMatch(incomingDoc -> existingDoc.getId() != null && existingDoc.getId().equals(incomingDoc.getId())))
                .collect(Collectors.toList());
        documentsToRemove.forEach(doc -> {
            existingOffre.getDocuments().remove(doc);
            if (doc.getCheminFichier() != null && !doc.getCheminFichier().isEmpty()) {
                fileStorageService.deleteFile(doc.getCheminFichier());
            }
        });
        for (DocumentOffre incomingDoc : incomingDocuments) {
            if (incomingDoc.getId() == null) {
                DocumentOffre newDoc = new DocumentOffre();
                newDoc.setNamefile(incomingDoc.getNamefile());
                newDoc.setDescription(incomingDoc.getDescription());
                newDoc.setType(incomingDoc.getType());
                if (incomingDoc.getCheminFichier() != null && !incomingDoc.getCheminFichier().isEmpty()) {
                    Optional<MultipartFile> matchingFile = files != null ? files.stream()
                            .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(incomingDoc.getCheminFichier()))
                            .findFirst() : Optional.empty();
                    if (matchingFile.isPresent()) {
                        String storedFileName = fileStorageService.storeFile(matchingFile.get());
                        newDoc.setCheminFichier(storedFileName);
                    } else {
                        System.err.println("Warning: New document metadata received without corresponding file: " + incomingDoc.getNamefile());
                        newDoc.setCheminFichier(null);
                    }
                }
                newDoc.setOffre(existingOffre);
                existingOffre.getDocuments().add(newDoc);
            } else {
                existingOffre.getDocuments().stream()
                        .filter(existingDoc -> existingDoc.getId().equals(incomingDoc.getId()))
                        .findFirst()
                        .ifPresent(existingDoc -> {
                            existingDoc.setNamefile(incomingDoc.getNamefile());
                            existingDoc.setDescription(incomingDoc.getDescription());
                            existingDoc.setType(incomingDoc.getType());
                            if (incomingDoc.getCheminFichier() != null && !incomingDoc.getCheminFichier().isEmpty()) {
                                Optional<MultipartFile> matchingFile = files != null ? files.stream()
                                        .filter(file -> file.getOriginalFilename() != null && file.getOriginalFilename().equals(incomingDoc.getCheminFichier()))
                                        .findFirst() : Optional.empty();
                                if (matchingFile.isPresent()) {
                                    String storedFileName = fileStorageService.storeFile(matchingFile.get());
                                    if (existingDoc.getCheminFichier() != null && !existingDoc.getCheminFichier().equals(storedFileName)) {
                                        fileStorageService.deleteFile(existingDoc.getCheminFichier());
                                    }
                                    existingDoc.setCheminFichier(storedFileName);
                                }
                            }
                        });
            }
        }

        // --- Handle tasks collection synchronization ---
        List<Tache> incomingTaches = offreDetails.getTaches() != null ?
                offreDetails.getTaches() : new ArrayList<>();
        List<Tache> tachesToRemove = existingOffre.getTaches().stream()
                .filter(existingTache -> incomingTaches.stream().noneMatch(incomingTache -> existingTache.getId() != null && existingTache.getId().equals(incomingTache.getId())))
                .collect(Collectors.toList());
        tachesToRemove.forEach(existingOffre.getTaches()::remove);
        for (Tache incomingTache : incomingTaches) {
            if (incomingTache.getId() == null) {
                Tache newTache = new Tache();
                newTache.setTitre(incomingTache.getTitre());
                newTache.setDetail(incomingTache.getDetail());
                newTache.setDeadline(incomingTache.getDeadline());
                newTache.setAssignedPerson(incomingTache.getAssignedPerson());
                newTache.setChecked(incomingTache.isChecked());
                newTache.setOffre(existingOffre);
                existingOffre.getTaches().add(newTache);
            } else {
                existingOffre.getTaches().stream()
                        .filter(existingTache -> existingTache.getId().equals(incomingTache.getId()))
                        .findFirst()
                        .ifPresent(existingTache -> {
                            existingTache.setTitre(incomingTache.getTitre());
                            existingTache.setDetail(incomingTache.getDetail());
                            existingTache.setDeadline(incomingTache.getDeadline());
                            existingTache.setAssignedPerson(incomingTache.getAssignedPerson());
                            existingTache.setChecked(incomingTache.isChecked());
                        });
            }
        }

        Offre updatedOffre = offreRepository.save(existingOffre);

        // Maintain bidirectional consistency for Opportunite
        if (oldOpportunite != null && (newOpportunite == null || !oldOpportunite.getIdOpp().equals(newOpportunite.getIdOpp()))) {
            // If the old opportunite is no longer linked or linked to a different one, break its link
            oldOpportunite.setOffre(null);
            opportuniteRepository.save(oldOpportunite);
        }
        if (newOpportunite != null) {
            newOpportunite.setOffre(updatedOffre);
            opportuniteRepository.save(newOpportunite); // Save new Opportunite to update its 'offre' field
        }

        return updatedOffre;
    }

    public void deleteOffre(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));

        // Before deleting the Offre, ensure the associated Opportunite's 'offre' field is nulled out
        if (offre.getOpportunite() != null) {
            Opportunite associatedOpportunite = offre.getOpportunite();
            associatedOpportunite.setOffre(null);
            opportuniteRepository.save(associatedOpportunite);
        }

        // Delete associated files before deleting the offer
        if (offre.getDocuments() != null) {
            offre.getDocuments().forEach(doc -> {
                if (doc.getCheminFichier() != null && !doc.getCheminFichier().isEmpty()) {
                    fileStorageService.deleteFile(doc.getCheminFichier());
                }
            });
        }
        offreRepository.delete(offre);
    }

    public Offre updateOffreStatus(Long id, Adjuge statut) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + id));
        offre.setAdjuge(statut);
        return offreRepository.save(offre);
    }

    // Methods to add/remove tasks and documents individually (for detail view editing)
    public Tache addTacheToOffre(Long offreId, Tache tache) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + offreId));
        tache.setOffre(offre);
        return tacheRepository.save(tache);
    }

    public void deleteTacheFromOffre(Long offreId, Long tacheId) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + offreId));
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'ID: " + tacheId));
        if (!tache.getOffre().getIdOffre().equals(offreId)) {
            throw new RuntimeException("La tâche n'appartient pas à cette offre.");
        }
        tacheRepository.delete(tache);
    }

    public DocumentOffre addDocumentToOffre(Long offreId, DocumentOffre document, MultipartFile file) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + offreId));
        if (file != null) {
            String storedFileName = fileStorageService.storeFile(file);
            document.setCheminFichier(storedFileName);
        }
        document.setOffre(offre);
        return documentOffreRepository.save(document);
    }

    public void deleteDocumentFromOffre(Long offreId, Long documentId) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + offreId));
        DocumentOffre document = documentOffreRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'ID: " + documentId));
        if (!document.getOffre().getIdOffre().equals(offreId)) {
            throw new RuntimeException("Le document n'appartient pas à cette offre.");
        }
        if (document.getCheminFichier() != null && !document.getCheminFichier().isEmpty()) {
            fileStorageService.deleteFile(document.getCheminFichier());
        }
        documentOffreRepository.delete(document);
    }
}
