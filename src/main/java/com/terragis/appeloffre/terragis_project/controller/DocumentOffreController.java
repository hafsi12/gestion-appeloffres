package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.DocumentOffre;
import com.terragis.appeloffre.terragis_project.service.DocumentOffreService;
import com.terragis.appeloffre.terragis_project.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentOffreController {
    
    private final DocumentOffreService documentOffreService;
    private final FileStorageService fileStorageService;

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            DocumentOffre document = documentOffreService.getDocumentById(id);
            
            if (document.getCheminFichier() == null || document.getCheminFichier().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = fileStorageService.loadFileAsResource(document.getCheminFichier());
            
            String contentType = determineContentType(document.getType());
            String filename = document.getNamefile() != null ? document.getNamefile() : "document_" + id;
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private String determineContentType(String type) {
        if (type == null) return "application/octet-stream";
        
        return switch (type.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls", "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }
}
