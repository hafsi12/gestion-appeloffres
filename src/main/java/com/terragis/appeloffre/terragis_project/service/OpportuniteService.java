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
    private final DocumentOpportuniteRepository documentRepository;
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

        if (opportunite.getIncomingClientId() != null) {
            MaitreOeuvrage client = clientRepository.findById(opportunite.getIncomingClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + opportunite.getIncomingClientId()));
            opportunite.setClient(client);
        }

        if (opportunite.getEtat() == null) {
            opportunite.setEtat(new EtatOpportunite());
            opportunite.getEtat().setStatut(EtatOpportuniteEnum.EN_COURS);
        }

        etatRepository.save(opportunite.getEtat());
        Opportunite savedOpportunite = opportuniteRepository.save(opportunite);

        if (opportunite.getDocuments() != null && !opportunite.getDocuments().isEmpty()) {
            opportunite.getDocuments().forEach(doc -> {
                doc.setOpportunite(savedOpportunite);
                documentRepository.save(doc);
            });
        }

        return savedOpportunite;
    }

    public Opportunite updateOpportunite(Long id, Opportunite opportuniteDetails) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));

        opportunite.setProjectName(opportuniteDetails.getProjectName());
        opportunite.setBudget(opportuniteDetails.getBudget());
        opportunite.setDeadline(opportuniteDetails.getDeadline());
        opportunite.setDescription(opportuniteDetails.getDescription());

        if (opportuniteDetails.getIncomingClientId() != null) {
            MaitreOeuvrage client = clientRepository.findById(opportuniteDetails.getIncomingClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + opportuniteDetails.getIncomingClientId()));
            opportunite.setClient(client);
        } else {
            opportunite.setClient(null);
        }

        if (opportuniteDetails.getEtat() != null) {
            EtatOpportunite etat = opportunite.getEtat();
            if (etat == null) {
                etat = new EtatOpportunite();
                etat.setOpportunite(opportunite);
            }
            etat.setStatut(opportuniteDetails.getEtat().getStatut());
            etat.setJustification(opportuniteDetails.getEtat().getJustification());
            etatRepository.save(etat);
            opportunite.setEtat(etat);
        }

        if (opportuniteDetails.getDocuments() != null) {
            documentRepository.deleteByOpportuniteId(opportunite.getIdOpp());
            for (DocumentOpportunite doc : opportuniteDetails.getDocuments()) {
                doc.setOpportunite(opportunite);
            }
            documentRepository.saveAll(opportuniteDetails.getDocuments());
        } else {
            documentRepository.deleteByOpportuniteId(opportunite.getIdOpp());
        }

        return opportuniteRepository.save(opportunite);
    }

    public void deleteOpportunite(Long id) {
        Opportunite opportunite = opportuniteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunité non trouvée"));

        documentRepository.deleteByOpportuniteId(id);

        if (opportunite.getEtat() != null) {
            etatRepository.delete(opportunite.getEtat());
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