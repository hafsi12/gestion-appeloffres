package com.terragis.appeloffre.terragis_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terragis.appeloffre.terragis_project.entity.Adjuge;
import com.terragis.appeloffre.terragis_project.entity.DocumentOffre;
import com.terragis.appeloffre.terragis_project.entity.Offre;
import com.terragis.appeloffre.terragis_project.entity.Tache;
import com.terragis.appeloffre.terragis_project.service.FileStorageService;
import com.terragis.appeloffre.terragis_project.service.OffreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "http://localhost:3000")
public class OffreController {
    private final OffreService offreService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public OffreController(OffreService offreService, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.offreService = offreService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Offre> getAllOffres() {
        return offreService.getAllOffres();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offre> getOffreById(@PathVariable Long id) {
        return offreService.getOffreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createOffre(
            @RequestPart("offre") String offreJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            Offre offre = objectMapper.readValue(offreJson, Offre.class);
            Offre createdOffre = offreService.createOffre(offre, files);
            return ResponseEntity.ok(createdOffre);
        } catch (Exception e) {
            System.err.println("Error creating offre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la création de l'offre: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateOffre(
            @PathVariable Long id,
            @RequestPart("offre") String offreJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            Offre offreDetails = objectMapper.readValue(offreJson, Offre.class);
            Offre updatedOffre = offreService.updateOffre(id, offreDetails, files);
            return ResponseEntity.ok(updatedOffre);
        } catch (RuntimeException e) {
            System.err.println("RuntimeException during update for ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            System.err.println("IOException during update for ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erreur de lecture/écriture: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error during update for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erreur inattendue: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long id) {
        try {
            offreService.deleteOffre(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Offre> updateOffreStatus(
            @PathVariable Long id,
            @RequestParam Adjuge statut) {
        try {
            Offre updatedOffre = offreService.updateOffreStatus(id, statut);
            return ResponseEntity.ok(updatedOffre);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints for managing tasks within an offer
    @PostMapping("/{offreId}/taches")
    public ResponseEntity<?> addTacheToOffre(@PathVariable Long offreId, @RequestBody Tache tache) {
        try {
            Tache createdTache = offreService.addTacheToOffre(offreId, tache);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTache);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{offreId}/taches/{tacheId}")
    public ResponseEntity<Void> deleteTacheFromOffre(@PathVariable Long offreId, @PathVariable Long tacheId) {
        try {
            offreService.deleteTacheFromOffre(offreId, tacheId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoints for managing documents within an offer
    @PostMapping(value = "/{offreId}/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addDocumentToOffre(
            @PathVariable Long offreId,
            @RequestPart("document") String documentJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            DocumentOffre document = objectMapper.readValue(documentJson, DocumentOffre.class);
            DocumentOffre createdDocument = offreService.addDocumentToOffre(offreId, document, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Erreur de lecture du document JSON: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{offreId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocumentFromOffre(@PathVariable Long offreId, @PathVariable Long documentId) {
        try {
            offreService.deleteDocumentFromOffre(offreId, documentId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoint to download files (reusing FileStorageService)
    @GetMapping("/documents/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
