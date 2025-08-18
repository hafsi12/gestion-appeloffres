package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.Facture;
import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import com.terragis.appeloffre.terragis_project.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    // Récupérer toutes les factures
    @GetMapping
    public ResponseEntity<?> getAllFactures() {
        try {
            List<Facture> factures = factureService.getAllFactures();
            return ResponseEntity.ok(factures);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur lors du chargement des factures", "message", e.getMessage()));
        }
    }

    // Récupérer une facture par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        try {
            return factureService.getFactureById(id)
                    .map(facture -> ResponseEntity.ok().body(facture))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur lors de la récupération", "message", e.getMessage()));
        }
    }

    // Créer une nouvelle facture
    @PostMapping
    public ResponseEntity<?> createFacture(@RequestBody Facture facture) {
        try {
            Facture createdFacture = factureService.createFacture(facture);
            return ResponseEntity.ok(createdFacture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la création de la facture: " + e.getMessage()));
        }
    }

    // Mettre à jour une facture
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFacture(@PathVariable Long id, @RequestBody Facture facture) {
        try {
            Facture updatedFacture = factureService.updateFacture(id, facture);
            return ResponseEntity.ok(updatedFacture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    // Supprimer une facture
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureService.deleteFacture(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Marquer une facture comme payée
    @PutMapping("/{id}/payer")
    public ResponseEntity<?> marquerCommePaye(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerCommePaye(id);
            return ResponseEntity.ok(facture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors du paiement: " + e.getMessage()));
        }
    }

    // Marquer une facture comme soldée
    @PutMapping("/{id}/solder")
    public ResponseEntity<?> marquerCommeSolde(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerCommeSolde(id);
            return ResponseEntity.ok(facture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors du soldage: " + e.getMessage()));
        }
    }

    // Récupérer les factures par contrat
    @GetMapping("/contrat/{contratId}")
    public ResponseEntity<List<Facture>> getFacturesByContrat(@PathVariable Long contratId) {
        List<Facture> factures = factureService.getFacturesByContrat(contratId);
        return ResponseEntity.ok(factures);
    }

    // Récupérer les factures par statut
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Facture>> getFacturesByStatut(@PathVariable StatutPaiement statut) {
        List<Facture> factures = factureService.getFacturesByStatut(statut);
        return ResponseEntity.ok(factures);
    }

    // Récupérer les factures par période
    @GetMapping("/periode")
    public ResponseEntity<List<Facture>> getFacturesByPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<Facture> factures = factureService.getFacturesByPeriode(startDate, endDate);
        return ResponseEntity.ok(factures);
    }

    // Récupérer les factures par client
    @GetMapping("/client")
    public ResponseEntity<List<Facture>> getFacturesByClient(@RequestParam String clientName) {
        List<Facture> factures = factureService.getFacturesByClient(clientName);
        return ResponseEntity.ok(factures);
    }

    // Générer une facture pour un contrat
    @PostMapping("/generer/{contratId}")
    public ResponseEntity<?> genererFacturePourContrat(@PathVariable Long contratId) {
        try {
            Facture facture = factureService.genererFacturePourContrat(contratId);
            return ResponseEntity.ok(facture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la génération: " + e.getMessage()));
        }
    }

    // Obtenir les statistiques des factures
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiquesFactures() {
        Map<String, Object> statistiques = factureService.getStatistiquesFactures();
        return ResponseEntity.ok(statistiques);
    }

    // Obtenir les statistiques mensuelles
    @GetMapping("/statistiques/mensuelles")
    public ResponseEntity<List<Map<String, Object>>> getStatistiquesMensuelles() {
        List<Map<String, Object>> statistiques = factureService.getStatistiquesMensuelles();
        return ResponseEntity.ok(statistiques);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getFactureDetails(@PathVariable Long id) {
        try {
            Map<String, Object> details = factureService.getFactureDetails(id);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Erreur lors de la récupération des détails", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/ticket/pdf")
    public ResponseEntity<byte[]> genererTicketPDF(@PathVariable Long id) {
        try {
            byte[] pdfBytes = factureService.genererTicketPDF(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-facture-" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/regenerer/{contratId}")
    public ResponseEntity<?> regenererFacture(@PathVariable Long contratId) {
        try {
            Facture facture = factureService.regenererFacture(contratId);
            return ResponseEntity.ok(facture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la régénération: " + e.getMessage()));
        }
    }

    @PostMapping("/contrat/{contratId}")
    public ResponseEntity<?> genererFactureContrat(@PathVariable Long contratId) {
        try {
            Facture facture = factureService.genererFacturePourContrat(contratId);
            return ResponseEntity.ok(facture);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la génération: " + e.getMessage()));
        }
    }
}
