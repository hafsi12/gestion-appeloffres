package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.MaitreOeuvrage;
import com.terragis.appeloffre.terragis_project.repository.MaitreOeuvrageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaitreOeuvrageService {
    private final MaitreOeuvrageRepository maitreOeuvrageRepository;

    public List<MaitreOeuvrage> getAllClients(boolean archived) {
        return maitreOeuvrageRepository.findByArchived(archived);
    }

    public MaitreOeuvrage getClientById(Long id) {
        return maitreOeuvrageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public MaitreOeuvrage createClient(MaitreOeuvrage client) {
        client.setArchived(false);
        return maitreOeuvrageRepository.save(client);
    }

    public MaitreOeuvrage updateClient(Long id, MaitreOeuvrage clientDetails) {
        MaitreOeuvrage existingClient = maitreOeuvrageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        existingClient.setName(clientDetails.getName());
        existingClient.setWebSite(clientDetails.getWebSite());
        existingClient.setAddress(clientDetails.getAddress());
        existingClient.setSecteur(clientDetails.getSecteur());
        return maitreOeuvrageRepository.save(existingClient);
    }

    // Modified to toggle the archived status and return the updated client
    public MaitreOeuvrage toggleArchiveStatus(Long id) {
        MaitreOeuvrage client = getClientById(id);
        client.setArchived(!client.isArchived()); // Toggle the status
        return maitreOeuvrageRepository.save(client);
    }

    public void deleteClient(Long id) {
        maitreOeuvrageRepository.deleteById(id);
    }
}
