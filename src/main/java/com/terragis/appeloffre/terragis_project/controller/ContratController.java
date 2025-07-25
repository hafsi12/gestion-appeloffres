package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.Contrat;
import com.terragis.appeloffre.terragis_project.entity.Livrable;
import com.terragis.appeloffre.terragis_project.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {
    private final ContratService contratService;

    @GetMapping
    public ResponseEntity<List<Contrat>> getAllContrats() {
        return ResponseEntity.ok(contratService.getAllContrats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contrat> getContratById(@PathVariable Long id) {
        return contratService.getContratById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createContrat(@RequestBody Contrat contrat) {
        try {
            Contrat createdContrat = contratService.createContrat(contrat);
            return ResponseEntity.ok(createdContrat);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la création du contrat: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContrat(@PathVariable Long id, @RequestBody Contrat contrat) {
        try {
            Contrat updatedContrat = contratService.updateContrat(id, contrat);
            return ResponseEntity.ok(updatedContrat);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContrat(@PathVariable Long id) {
        try {
            contratService.deleteContrat(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<?> signContrat(@PathVariable Long id, @RequestBody Map<String, String> signatureData) {
        try {
            String signature = signatureData.get("signature");
            String signerName = signatureData.get("signerName");
            Contrat signedContrat = contratService.signContrat(id, signature, signerName);
            return ResponseEntity.ok(signedContrat);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la signature: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/generate-pdf")
    public ResponseEntity<Resource> generateContratPDF(@PathVariable Long id) {
        try {
            Contrat contrat = contratService.getContratById(id)
                    .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

            Resource pdfResource = contratService.generateContratPDF(id);

            String filename = String.format("contrat_%d_%s.html",
                    id,
                    contrat.getNameClient().replaceAll("[^a-zA-Z0-9]", "_"));

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(pdfResource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendContratByEmail(@PathVariable Long id) {
        try {
            Map<String, Object> result = contratService.sendContratByEmail(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de l'envoi: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/generate-and-send")
    public ResponseEntity<?> generateAndSendContrat(@PathVariable Long id) {
        try {
            Map<String, Object> result = contratService.generateAndSendContratPDF(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de l'envoi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/livrables")
    public ResponseEntity<List<Livrable>> getContratLivrables(@PathVariable Long id) {
        List<Livrable> livrables = contratService.getContratLivrables(id);
        return ResponseEntity.ok(livrables);
    }

    @PostMapping("/{id}/livrables")
    public ResponseEntity<?> addLivrable(@PathVariable Long id, @RequestBody Livrable livrable) {
        try {
            Livrable createdLivrable = contratService.addLivrable(id, livrable);
            return ResponseEntity.ok(createdLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de l'ajout du livrable: " + e.getMessage()));
        }
    }

    @PutMapping("/{contratId}/livrables/{livrableId}")
    public ResponseEntity<?> updateLivrable(
            @PathVariable Long contratId,
            @PathVariable Long livrableId,
            @RequestBody Livrable livrable) {
        try {
            Livrable updatedLivrable = contratService.updateLivrable(contratId, livrableId, livrable);
            return ResponseEntity.ok(updatedLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{contratId}/livrables/{livrableId}")
    public ResponseEntity<Void> deleteLivrable(@PathVariable Long contratId, @PathVariable Long livrableId) {
        try {
            contratService.deleteLivrable(contratId, livrableId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
