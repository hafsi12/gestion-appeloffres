package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.Contrat;
import com.terragis.appeloffre.terragis_project.entity.Facture;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import com.terragis.appeloffre.terragis_project.repository.ContratRepository;
import com.terragis.appeloffre.terragis_project.repository.FactureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FactureService {

    private final FactureRepository factureRepository;
    private final ContratRepository contratRepository;
    private final PDFService pdfService;

    // Créer une nouvelle facture
    public Facture createFacture(Facture facture) {
        // Vérifier que le contrat existe
        if (facture.getContrat() != null && facture.getContrat().getId() != null) {
            Contrat contrat = contratRepository.findById(facture.getContrat().getId())
                    .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
            facture.setContrat(contrat);
        }

        // Générer le numéro de facture si non fourni
        if (facture.getNumeroFacture() == null || facture.getNumeroFacture().isEmpty()) {
            facture.setNumeroFacture(generateNumeroFacture());
        }

        // Définir la date de facture si non fournie
        if (facture.getDateFacture() == null) {
            facture.setDateFacture(new Date());
        }

        // Calculer le montant total basé sur les livrables du contrat
        if (facture.getMontantTotal() == null) {
            facture.setMontantTotal(facture.calculateMontantTotal());
        }

        // Définir le statut initial si non fourni
        if (facture.getStatutFacture() == null) {
            facture.setStatutFacture(StatutPaiement.NON_PAYE);
        }

        return factureRepository.save(facture);
    }

    // Mettre à jour une facture
    public Facture updateFacture(Long id, Facture factureDetails) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        // Mettre à jour les champs modifiables
        if (factureDetails.getDateFacture() != null) {
            facture.setDateFacture(factureDetails.getDateFacture());
        }
        if (factureDetails.getMontantTotal() != null) {
            facture.setMontantTotal(factureDetails.getMontantTotal());
        }
        if (factureDetails.getStatutFacture() != null) {
            facture.setStatutFacture(factureDetails.getStatutFacture());
        }

        return factureRepository.save(facture);
    }

    // Marquer une facture comme payée
    public Facture marquerCommePaye(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        facture.setStatutFacture(StatutPaiement.PAYE);
        return factureRepository.save(facture);
    }

    // Marquer une facture comme soldée
    public Facture marquerCommeSolde(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        facture.setStatutFacture(StatutPaiement.SOLDE);
        return factureRepository.save(facture);
    }

    // Récupérer toutes les factures
    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }

    // Récupérer une facture par ID
    public Optional<Facture> getFactureById(Long id) {
        return factureRepository.findById(id);
    }

    // Récupérer les factures par contrat
    public List<Facture> getFacturesByContrat(Long contratId) {
        return factureRepository.findByContratId(contratId);
    }

    // Récupérer les factures par statut
    public List<Facture> getFacturesByStatut(StatutPaiement statut) {
        return factureRepository.findByStatutPaiement(statut);
    }

    // Récupérer les factures par période
    public List<Facture> getFacturesByPeriode(Date startDate, Date endDate) {
        return factureRepository.findByDateFactureBetween(startDate, endDate);
    }

    // Récupérer les factures par client
    public List<Facture> getFacturesByClient(String clientName) {
        return factureRepository.findByClientName(clientName);
    }

    // Supprimer une facture
    public void deleteFacture(Long id) {
        if (!factureRepository.existsById(id)) {
            throw new RuntimeException("Facture non trouvée");
        }
        factureRepository.deleteById(id);
    }

    // Générer automatiquement une facture pour un contrat
    public Facture genererFacturePourContrat(Long contratId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        Facture facture = new Facture();
        facture.setContrat(contrat);
        facture.setNumeroFacture(generateNumeroFacture());
        facture.setDateFacture(new Date());
        facture.setMontantTotal(facture.calculateMontantTotal());
        facture.setStatutFacture(facture.calculateStatutFacture());

        return factureRepository.save(facture);
    }

    // Obtenir les statistiques des factures
    public Map<String, Object> getStatistiquesFactures() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de factures
        long totalFactures = factureRepository.count();
        stats.put("totalFactures", totalFactures);

        // Montant total payé
        Double montantPaye = factureRepository.getTotalMontantPaye();
        stats.put("montantTotalPaye", montantPaye != null ? montantPaye : 0.0);

        // Montant total non payé
        Double montantNonPaye = factureRepository.getTotalMontantNonPaye();
        stats.put("montantTotalNonPaye", montantNonPaye != null ? montantNonPaye : 0.0);

        // Factures par statut
        stats.put("facturesPayees", factureRepository.findByStatutPaiement(StatutPaiement.PAYE).size());
        stats.put("facturesNonPayees", factureRepository.findByStatutPaiement(StatutPaiement.NON_PAYE).size());
        stats.put("facturesSoldees", factureRepository.findByStatutPaiement(StatutPaiement.SOLDE).size());

        // Factures en retard (plus de 30 jours)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        List<Facture> facturesEnRetard = factureRepository.findFacturesEnRetard(cal.getTime());
        stats.put("facturesEnRetard", facturesEnRetard.size());

        return stats;
    }

    // Obtenir les statistiques mensuelles
    public List<Map<String, Object>> getStatistiquesMensuelles() {
        List<Object[]> results = factureRepository.getStatistiquesMensuelles();
        List<Map<String, Object>> statistiques = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("annee", result[0]);
            stat.put("mois", result[1]);
            stat.put("nombreFactures", result[2]);
            stat.put("montantTotal", result[3]);
            statistiques.add(stat);
        }

        return statistiques;
    }

    // Générer un numéro de facture unique
    private String generateNumeroFacture() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String datePrefix = sdf.format(new Date());

        // Compter les factures du mois actuel
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfMonth = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        Date startOfNextMonth = cal.getTime();

        List<Facture> facturesDuMois = factureRepository.findByDateFactureBetween(startOfMonth, startOfNextMonth);
        int numeroSequentiel = facturesDuMois.size() + 1;

        return String.format("FACT-%s-%04d", datePrefix, numeroSequentiel);
    }

    public Map<String, Object> getFactureDetails(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        Map<String, Object> details = new HashMap<>();
        details.put("facture", facture);
        details.put("contrat", facture.getContrat());
        details.put("livrables", facture.getContrat().getLivrables());
        details.put("montantCalcule", facture.calculateMontantTotal());
        details.put("statutCalcule", facture.calculateStatutFacture());

        return details;
    }

    public Facture regenererFacture(Long contratId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        // Supprimer l'ancienne facture si elle existe
        List<Facture> anciensFactures = factureRepository.findByContratId(contratId);
        for (Facture ancienneFacture : anciensFactures) {
            factureRepository.delete(ancienneFacture);
        }

        // Créer une nouvelle facture avec les données actualisées
        Facture nouvelleFacture = new Facture();
        nouvelleFacture.setContrat(contrat);
        nouvelleFacture.setNumeroFacture(generateNumeroFacture());
        nouvelleFacture.setDateFacture(new Date());
        nouvelleFacture.setMontantTotal(nouvelleFacture.calculateMontantTotal());
        nouvelleFacture.setStatutFacture(nouvelleFacture.calculateStatutFacture());

        return factureRepository.save(nouvelleFacture);
    }

    public byte[] genererTicketPDF(Long factureId) throws Exception {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        // Vérifier que la facture est payée
        if (facture.getStatutFacture() != StatutPaiement.PAYE &&
                facture.getStatutFacture() != StatutPaiement.SOLDE) {
            throw new RuntimeException("La facture doit être payée pour générer un ticket");
        }

        return pdfService.genererTicketPDF(facture);
    }
}
