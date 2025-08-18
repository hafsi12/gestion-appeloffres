package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.Livrable;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import com.terragis.appeloffre.terragis_project.entity.StatutValidation;
import com.terragis.appeloffre.terragis_project.repository.LivrableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LivrableService {
    
    private final LivrableRepository livrableRepository;

    public List<Livrable> getAllLivrables() {
        return livrableRepository.findAll();
    }

    public Optional<Livrable> getLivrableById(Long id) {
        return livrableRepository.findById(id);
    }

    public Livrable createLivrable(Livrable livrable) {
        if (livrable.getStatutValidation() == null) {
            livrable.setStatutValidation(StatutValidation.EN_ATTENTE);
        }
        if (livrable.getStatutPaiement() == null) {
            livrable.setStatutPaiement(StatutPaiement.NON_PAYE);
        }
        return livrableRepository.save(livrable);
    }

    public Livrable updateLivrable(Long id, Livrable livrableDetails) {
        Livrable livrable = livrableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livrable non trouvé avec l'ID: " + id));

        if (livrableDetails.getTitre() != null) {
            livrable.setTitre(livrableDetails.getTitre());
        }
        if (livrableDetails.getDescription() != null) {
            livrable.setDescription(livrableDetails.getDescription());
        }
        if (livrableDetails.getMontant() != null) {
            livrable.setMontant(livrableDetails.getMontant());
        }
        if (livrableDetails.getDateLivraison() != null) {
            livrable.setDateLivraison(livrableDetails.getDateLivraison());
        }
        if (livrableDetails.getStatutValidation() != null) {
            livrable.setStatutValidation(livrableDetails.getStatutValidation());
        }
        if (livrableDetails.getStatutPaiement() != null) {
            livrable.setStatutPaiement(livrableDetails.getStatutPaiement());
        }
        if (livrableDetails.getFichierJoint() != null) {
            livrable.setFichierJoint(livrableDetails.getFichierJoint());
        }

        return livrableRepository.save(livrable);
    }

    public Livrable updateStatutValidation(Long id, String statut) {
        Livrable livrable = livrableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livrable non trouvé avec l'ID: " + id));
        
        livrable.setStatutValidation(StatutValidation.valueOf(statut));
        return livrableRepository.save(livrable);
    }

    public Livrable updateStatutPaiement(Long id, String statut) {
        Livrable livrable = livrableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livrable non trouvé avec l'ID: " + id));
        
        livrable.setStatutPaiement(StatutPaiement.valueOf(statut));
        return livrableRepository.save(livrable);
    }

    public void deleteLivrable(Long id) {
        if (!livrableRepository.existsById(id)) {
            throw new RuntimeException("Livrable non trouvé avec l'ID: " + id);
        }
        livrableRepository.deleteById(id);
    }

    public List<Livrable> getLivrablesByContrat(Long contratId) {
        return livrableRepository.findByContratId(contratId);
    }

    public Double getTotalMontantByContrat(Long contratId) {
        return livrableRepository.getTotalMontantByContrat(contratId);
    }

    public Long countByContrat(Long contratId) {
        return livrableRepository.countByContratId(contratId);
    }
}
