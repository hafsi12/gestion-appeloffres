package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.*;
import com.terragis.appeloffre.terragis_project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private MaitreOeuvrageRepository clientRepository;
    
    @Autowired
    private OpportuniteRepository opportuniteRepository;
    
    @Autowired
    private OffreRepository offreRepository;
    
    @Autowired
    private ContratRepository contratRepository;
    
    @Autowired
    private FactureRepository factureRepository;
    
    @Autowired
    private LivrableRepository livrableRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Statistiques générales
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalClients", clientRepository.count());
        stats.put("totalOpportunites", opportuniteRepository.count());
        stats.put("totalOffres", offreRepository.count());
        stats.put("totalContrats", contratRepository.count());
        stats.put("totalFactures", factureRepository.count());
        stats.put("totalLivrables", livrableRepository.count());
        stats.put("totalUsers", userRepository.count());
        
        return ResponseEntity.ok(stats);
    }

    // Répartition des opportunités par état
    @GetMapping("/opportunites/by-status")
    public ResponseEntity<Map<String, Object>> getOpportunitiesByStatus() {
        List<Opportunite> opportunites = opportuniteRepository.findAll();
        Map<String, Long> statusCount = new HashMap<>();
        
        for (Opportunite opp : opportunites) {
            String status = opp.getEtat() != null ? 
                opp.getEtat().getStatut().toString() : "NON_DEFINI";
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(statusCount.keySet()));
        result.put("data", new ArrayList<>(statusCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Répartition des offres par statut adjugé
    @GetMapping("/offres/by-adjuge")
    public ResponseEntity<Map<String, Object>> getOffresByAdjuge() {
        List<Offre> offres = offreRepository.findAll();
        Map<String, Long> adjugeCount = new HashMap<>();
        
        for (Offre offre : offres) {
            String adjuge = offre.getAdjuge() != null ? 
                offre.getAdjuge().toString() : "NON_DEFINI";
            adjugeCount.put(adjuge, adjugeCount.getOrDefault(adjuge, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(adjugeCount.keySet()));
        result.put("data", new ArrayList<>(adjugeCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Evolution mensuelle des opportunités
    @GetMapping("/opportunites/monthly-trend")
    public ResponseEntity<Map<String, Object>> getOpportunitesMonthlyTrend() {
        List<Opportunite> opportunites = opportuniteRepository.findAll();
        Map<String, Long> monthlyCount = new TreeMap<>();
        
        for (Opportunite opp : opportunites) {
            if (opp.getDeadline() != null) {
                LocalDate date = opp.getDeadline().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyCount.put(monthKey, monthlyCount.getOrDefault(monthKey, 0L) + 1);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(monthlyCount.keySet()));
        result.put("data", new ArrayList<>(monthlyCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Analyse des contrats par statut
    @GetMapping("/contrats/by-status")
    public ResponseEntity<Map<String, Object>> getContratsByStatus() {
        List<Contrat> contrats = contratRepository.findAll();
        Map<String, Long> statusCount = new HashMap<>();
        
        for (Contrat contrat : contrats) {
            String status = contrat.getStatut() != null ? 
                contrat.getStatut() : "NON_DEFINI";
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(statusCount.keySet()));
        result.put("data", new ArrayList<>(statusCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Analyse des factures par statut de paiement
    @GetMapping("/factures/by-payment-status")
    public ResponseEntity<Map<String, Object>> getFacturesByPaymentStatus() {
        List<Facture> factures = factureRepository.findAll();
        Map<String, Long> paymentCount = new HashMap<>();
        
        for (Facture facture : factures) {
            String status = facture.getStatutFacture() != null ? 
                facture.getStatutFacture().toString() : "NON_DEFINI";
            paymentCount.put(status, paymentCount.getOrDefault(status, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(paymentCount.keySet()));
        result.put("data", new ArrayList<>(paymentCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Analyse des montants par mois
    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue() {
        List<Facture> factures = factureRepository.findAll();
        Map<String, Double> monthlyRevenue = new TreeMap<>();
        
        for (Facture facture : factures) {
            if (facture.getDateFacture() != null && facture.getMontantTotal() != null) {
                LocalDate date = facture.getDateFacture().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyRevenue.put(monthKey, 
                    monthlyRevenue.getOrDefault(monthKey, 0.0) + facture.getMontantTotal());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(monthlyRevenue.keySet()));
        result.put("data", new ArrayList<>(monthlyRevenue.values()));
        
        return ResponseEntity.ok(result);
    }

    // Top 10 des clients par nombre d'opportunités
    @GetMapping("/clients/top-opportunities")
    public ResponseEntity<Map<String, Object>> getTopClientsByOpportunities() {
        List<Opportunite> opportunites = opportuniteRepository.findAll();
        Map<String, Long> clientOpportunityCount = new HashMap<>();
        
        for (Opportunite opp : opportunites) {
            if (opp.getClient() != null) {
                String clientName = opp.getClient().getName();
                clientOpportunityCount.put(clientName, 
                    clientOpportunityCount.getOrDefault(clientName, 0L) + 1);
            }
        }
        
        // Trier et prendre les 10 premiers
        List<Map.Entry<String, Long>> sortedEntries = clientOpportunityCount.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", sortedEntries.stream()
            .map(Map.Entry::getKey).collect(Collectors.toList()));
        result.put("data", sortedEntries.stream()
            .map(Map.Entry::getValue).collect(Collectors.toList()));
        
        return ResponseEntity.ok(result);
    }

    // Analyse des livrables par statut de validation
    @GetMapping("/livrables/by-validation-status")
    public ResponseEntity<Map<String, Object>> getLivrablesByValidationStatus() {
        List<Livrable> livrables = livrableRepository.findAll();
        Map<String, Long> validationCount = new HashMap<>();
        
        for (Livrable livrable : livrables) {
            String status = livrable.getStatutValidation() != null ? 
                livrable.getStatutValidation().toString() : "NON_DEFINI";
            validationCount.put(status, validationCount.getOrDefault(status, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(validationCount.keySet()));
        result.put("data", new ArrayList<>(validationCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Analyse des utilisateurs par rôle
    @GetMapping("/users/by-role")
    public ResponseEntity<Map<String, Object>> getUsersByRole() {
        List<User> users = userRepository.findAll();
        Map<String, Long> roleCount = new HashMap<>();
        
        for (User user : users) {
            String role = user.getRole() != null ? 
                user.getRole().toString() : "NON_DEFINI";
            roleCount.put(role, roleCount.getOrDefault(role, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", new ArrayList<>(roleCount.keySet()));
        result.put("data", new ArrayList<>(roleCount.values()));
        
        return ResponseEntity.ok(result);
    }

    // Statistiques avancées pour la prise de décision
    @GetMapping("/advanced-analytics")
    public ResponseEntity<Map<String, Object>> getAdvancedAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Taux de conversion opportunité -> offre
        long totalOpportunites = opportuniteRepository.count();
        long totalOffres = offreRepository.count();
        double conversionRate = totalOpportunites > 0 ? 
            (double) totalOffres / totalOpportunites * 100 : 0;
        
        // Taux de réussite des offres
        List<Offre> offres = offreRepository.findAll();
        long offresGagnees = offres.stream()
            .mapToLong(o -> o.getAdjuge() == Adjuge.GAGNEE ? 1 : 0).sum();
        double successRate = totalOffres > 0 ? 
            (double) offresGagnees / totalOffres * 100 : 0;
        
        // Montant moyen des contrats
        List<Facture> factures = factureRepository.findAll();
        double averageContractValue = factures.stream()
            .filter(f -> f.getMontantTotal() != null)
            .mapToDouble(Facture::getMontantTotal)
            .average().orElse(0.0);
        
        // Temps moyen de traitement (approximation)
        analytics.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
        analytics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        analytics.put("averageContractValue", Math.round(averageContractValue * 100.0) / 100.0);
        analytics.put("totalRevenue", factures.stream()
            .filter(f -> f.getMontantTotal() != null)
            .mapToDouble(Facture::getMontantTotal).sum());
        
        return ResponseEntity.ok(analytics);
    }
}