package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.Livrable;
import com.terragis.appeloffre.terragis_project.service.LivrableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/livrables")
@RequiredArgsConstructor
@Slf4j
public class LivrableController {
    
    private final LivrableService livrableService;

    @GetMapping
    public ResponseEntity<List<Livrable>> getAllLivrables() {
        List<Livrable> livrables = livrableService.getAllLivrables();
        return ResponseEntity.ok(livrables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livrable> getLivrableById(@PathVariable Long id) {
        return livrableService.getLivrableById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createLivrable(@RequestBody Livrable livrable) {
        try {
            Livrable createdLivrable = livrableService.createLivrable(livrable);
            return ResponseEntity.ok(createdLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la création du livrable: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLivrable(@PathVariable Long id, @RequestBody Livrable livrable) {
        try {
            Livrable updatedLivrable = livrableService.updateLivrable(id, livrable);
            return ResponseEntity.ok(updatedLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour du livrable: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLivrable(@PathVariable Long id) {
        try {
            livrableService.deleteLivrable(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/statut-validation")
    public ResponseEntity<?> updateStatutValidation(@PathVariable Long id, @RequestBody Map<String, String> statutData) {
        try {
            String statut = statutData.get("statutValidation");
            Livrable updatedLivrable = livrableService.updateStatutValidation(id, statut);
            return ResponseEntity.ok(updatedLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour du statut: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/statut-paiement")
    public ResponseEntity<?> updateStatutPaiement(@PathVariable Long id, @RequestBody Map<String, String> statutData) {
        try {
            String statut = statutData.get("statutPaiement");
            Livrable updatedLivrable = livrableService.updateStatutPaiement(id, statut);
            return ResponseEntity.ok(updatedLivrable);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erreur lors de la mise à jour du statut: " + e.getMessage()));
        }
    }
}
