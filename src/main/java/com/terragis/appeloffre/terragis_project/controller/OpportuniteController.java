package com.terragis.appeloffre.terragis_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import com.terragis.appeloffre.terragis_project.entity.EtatOpportuniteEnum;
import com.terragis.appeloffre.terragis_project.entity.Opportunite;
import com.terragis.appeloffre.terragis_project.service.FileStorageService; // Import FileStorageService
import com.terragis.appeloffre.terragis_project.service.OpportuniteService;
import org.springframework.core.io.Resource; // Import Resource
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opportunites")
@CrossOrigin(origins = "http://localhost:3000")
public class OpportuniteController {
    private final OpportuniteService opportuniteService;
    private final FileStorageService fileStorageService; // Inject FileStorageService
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    public OpportuniteController(OpportuniteService opportuniteService,
                                 FileStorageService fileStorageService, // Add FileStorageService to constructor
                                 ObjectMapper objectMapper) { // Add ObjectMapper to constructor
        this.opportuniteService = opportuniteService;
        this.fileStorageService = fileStorageService; // Initialize
        this.objectMapper = objectMapper; // Initialize
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

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}) // Consume multipart form data
    public ResponseEntity<?> createOpportunite(
            @RequestPart("opportunite") String opportuniteJson, // JSON part
            @RequestPart(value = "files", required = false) List<MultipartFile> files) { // Files part
        try {
            Opportunite opportunite = objectMapper.readValue(opportuniteJson, Opportunite.class); // Convert JSON string to object
            Opportunite createdOpportunite = opportuniteService.createOpportunite(opportunite, files);
            return ResponseEntity.ok(createdOpportunite);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Opportunite> updateOpportunite(
            @RequestParam Long id,
            @RequestPart("opportunite") String opportuniteJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            System.out.println("OpportuniteController: Received PUT request for ID from URL: " + id); // Log ID from URL
            Opportunite opportuniteDetails = objectMapper.readValue(opportuniteJson, Opportunite.class); // Convert JSON string to object
            System.out.println("OpportuniteController: Parsed opportunity JSON. Project Name: " + opportuniteDetails.getProjectName() + ", ID from JSON payload: " + opportuniteDetails.getIdOpp()); // Log ID from JSON payload
            Opportunite updatedOpportunite = opportuniteService.updateOpportunite(id, opportuniteDetails, files);
            return ResponseEntity.ok(updatedOpportunite);
        } catch (RuntimeException e) {
            System.err.println("OpportuniteController: RuntimeException during update for ID " + id + ": " + e.getMessage()); // Log RuntimeException
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            System.err.println("OpportuniteController: IOException during update for ID " + id + ": " + e.getMessage()); // Log IOException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTION_OFFRES') or hasRole('GESTION_CLIENTS_OPPORTUNITES') or hasRole('GESTION_CONTRATS')")
    @GetMapping("/go-disponibles")
    public List<Opportunite> getOpportunitesGoDisponibles() {
        return opportuniteService.getOpportunitesGoSansOffre();
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

    // New endpoint to download files
    @GetMapping("/documents/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Fallback to the default content type if type could not be determined
            System.out.println("Could not determine file type.");
        }

        // Fallback to default content type if not found
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
