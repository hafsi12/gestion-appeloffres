package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.MaitreOeuvrage;
import com.terragis.appeloffre.terragis_project.service.MaitreOeuvrageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class MaitreOeuvrageController {
    private final MaitreOeuvrageService clientService;

    @GetMapping
    public ResponseEntity<List<MaitreOeuvrage>> getAllClients(
            @RequestParam(required = false) Boolean archived) {
        boolean showArchived = archived != null && archived;
        return ResponseEntity.ok(clientService.getAllClients(showArchived));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaitreOeuvrage> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    public ResponseEntity<MaitreOeuvrage> createClient(@RequestBody MaitreOeuvrage client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaitreOeuvrage> updateClient(
            @PathVariable Long id, @RequestBody MaitreOeuvrage clientDetails) {
        return ResponseEntity.ok(clientService.updateClient(id, clientDetails));
    }

    // Modified to toggle the archive status and return the updated client
    @PatchMapping("/{id}/archive") // Keeping the same endpoint path
    public ResponseEntity<MaitreOeuvrage> toggleArchiveClient(@PathVariable Long id) {
        MaitreOeuvrage updatedClient = clientService.toggleArchiveStatus(id);
        return ResponseEntity.ok(updatedClient); // Return the updated client
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
