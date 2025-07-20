package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.Contact;
import com.terragis.appeloffre.terragis_project.entity.MaitreOeuvrage;
import com.terragis.appeloffre.terragis_project.repository.ContactRepository;
import com.terragis.appeloffre.terragis_project.repository.MaitreOeuvrageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final MaitreOeuvrageRepository maitreOeuvrageRepository;

    public List<Contact> getContactsByClientId(Long clientId) {
        return contactRepository.findByClientIdClient(clientId);
    }

    public Contact createContact(Long clientId, Contact contact) {
        MaitreOeuvrage client = maitreOeuvrageRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        contact.setClient(client);
        return contactRepository.save(contact);
    }
}
