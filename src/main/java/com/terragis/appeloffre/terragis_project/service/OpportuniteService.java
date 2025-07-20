package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.*;
import com.terragis.appeloffre.terragis_project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OpportuniteService {
    private final OpportuniteRepository opportuniteRepository;
    private final DocumentOpportuniteRepository documentRepository; // Still needed for custom delete if not using orphanRemoval fully
    private final EtatOpportuniteRepository etatRepository;
    private final MaitreOeuvrageRepository clientRepository;

    public OpportuniteService(OpportuniteRepository opportuniteRepository,
                              DocumentOpportuniteRepository documentRepository,
                              EtatOpportuniteRepository etatRepository,
                              MaitreOeuvrageRepository clientRepository) {
        this.opportuniteRepository = opportuniteRepository;
        this.documentRepository = documentRepository;
        this.etatRepository = etatRepository;
        this.clientRepository = clientRepository;
    }

    public List<Opportunite> getAllOpportunites() {
        return opportuniteRepository.findAll();
    }

    public Optional<Opportunite> getOpportuniteById(Long id) {
        return opportuniteRepository.findById(id);
    }

    public Opportunite createOpportunite(Opportunite opportunite) {
        System.out.println("Creating opportunity with client ID: " + opportunite.getIncomingClientId());

        // Set client if provided
        if (opportunite.getIncomingClientId() != null) {
            MaitreOeuvrage client = clientRepository.findById(opportunite.getIncomingClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + opportunite.getIncomingClientId()));
            opportunite.setClient(client);
        }

        // Initialize EtatOpportunite if null
        if (opportunite.getEtat() == null) {
            opportunite.setEtat(new EtatOpportunite());
            opportunite.getEtat().setStatut(EtatOpportuniteEnum.EN_COURS);
        }
        // Ensure the EtatOpportunite is linked back to the Opportunite if it's a new instance
        opportunite.getEtat().setOpportunite(opportunite);
        etatRepository.save(opportunite.getEtat()); // Save EtatOpportunite explicitly if not cascaded from Opportunite

        // IMPORTANT: Set the parent reference on documents BEFORE saving the opportunity
        if (opportunite.getDocuments() != null && !opportunite.getDocuments().isEmpty()) {
            opportunite.getDocuments().forEach(doc -> {
                doc.setOpportunite(opportunite); // Set the parent reference
            });
        }

        // Now save the opportunity. Due to cascade = CascadeType.ALL on 'documents' and 'etat',
        // associated documents and state will be persisted automatically.
        Opportunite savedOpportunite = opportuniteRepository.save(opportunite);

        return savedOpportunite;
    }

    public Opportunite updateOpportunite(Long id, Opportunite opportuniteDetails) {
        Opportunite existingOpportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));

        // Update basic fields
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
                etat.setOpportunite(existingOpportunite); // Link back to parent
            }
            etat.setStatut(opportuniteDetails.getEtat().getStatut());
            etat.setJustification(opportuniteDetails.getEtat().getJustification());
            etatRepository.save(etat); // Save the state explicitly if not cascaded from Opportunite
            existingOpportunite.setEtat(etat);
        }

        // Handle documents: This is the key change for updates.
        // Clear existing documents and add new ones. orphanRemoval=true will delete old ones.
        // This effectively replaces the entire collection.
        existingOpportunite.getDocuments().clear();
        if (opportuniteDetails.getDocuments() != null) {
            for (DocumentOpportunite doc : opportuniteDetails.getDocuments()) {
                doc.setOpportunite(existingOpportunite); // Set the parent reference
                existingOpportunite.getDocuments().add(doc); // Add to the collection
            }
        }

        return opportuniteRepository.save(existingOpportunite); // Save the parent, cascades will handle children
    }

    public void deleteOpportunite(Long id) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));

        // Due to cascade = CascadeType.ALL and orphanRemoval = true on 'documents',
        // and cascade = CascadeType.ALL on 'etat', deleting the parent opportunite
        // should automatically handle associated documents and etat.
        opportuniteRepository.delete(opportunite);
    }

    public Opportunite updateOpportuniteStatus(Long id, EtatOpportuniteEnum statut, String justification) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));

        EtatOpportunite etat = opportunite.getEtat();
        if (etat == null) {
            etat = new EtatOpportunite();
            etat.setOpportunite(opportunite); // Link back to parent
        }
        etat.setStatut(statut);
        etat.setJustification(statut == EtatOpportuniteEnum.NO_GO ? justification : null);
        opportunite.setEtat(etatRepository.save(etat)); // Save the state explicitly

        return opportuniteRepository.save(opportunite);
    }
}
