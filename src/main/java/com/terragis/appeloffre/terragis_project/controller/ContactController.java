package com.terragis.appeloffre.terragis_project.controller;

import com.terragis.appeloffre.terragis_project.entity.Contact;
import com.terragis.appeloffre.terragis_project.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // CRITICAL: Allow requests from your frontend
@RestController
@RequestMapping("/api/clients/{clientId}/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<List<Contact>> getContactsForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contactService.getContactsByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<Contact> createContactForClient(
            @PathVariable Long clientId, @RequestBody Contact contact) {
        return ResponseEntity.ok(contactService.createContact(clientId, contact));
    }
}
