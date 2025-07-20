package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.EtatOpportuniteEnum;
import com.terragis.appeloffre.terragis_project.entity.Opportunite;
import com.terragis.appeloffre.terragis_project.service.OpportuniteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opportunites")
@CrossOrigin(origins = "http://localhost:3000")
public class OpportuniteController {
    private final OpportuniteService opportuniteService;

    public OpportuniteController(OpportuniteService opportuniteService) {
        this.opportuniteService = opportuniteService;
    }

    @GetMapping
    public List<Opportunite> getAllOpportunites() {
        return opportuniteService.getAllOpportunites();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Opportunite> getOpportuniteById(@PathVariable Long id) {
        return opportuniteService.getOpportuniteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOpportunite(@RequestBody Opportunite opportunite) {
        try {
            Opportunite createdOpportunite = opportuniteService.createOpportunite(opportunite);
            return ResponseEntity.ok(createdOpportunite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Opportunite> updateOpportunite(@PathVariable Long id, @RequestBody Opportunite opportuniteDetails) {
        try {
            Opportunite updatedOpportunite = opportuniteService.updateOpportunite(id, opportuniteDetails);
            return ResponseEntity.ok(updatedOpportunite);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOpportunite(@PathVariable Long id) {
        try {
            opportuniteService.deleteOpportunite(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Opportunite> updateStatut(
            @PathVariable Long id,
            @RequestParam EtatOpportuniteEnum statut,
            @RequestParam(required = false) String justification) {
        try {
            Opportunite updatedOpportunite = opportuniteService.updateOpportuniteStatus(id, statut, justification);
            return ResponseEntity.ok(updatedOpportunite);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}