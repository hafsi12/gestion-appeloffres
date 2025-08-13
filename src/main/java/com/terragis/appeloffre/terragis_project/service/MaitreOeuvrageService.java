package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.MaitreOeuvrage;
import com.terragis.appeloffre.terragis_project.repository.MaitreOeuvrageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MaitreOeuvrageService {
    private final MaitreOeuvrageRepository maitreOeuvrageRepository;
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final Random random = new Random();

    public List<MaitreOeuvrage> getAllClients(boolean archived) {
        return maitreOeuvrageRepository.findByArchived(archived);
    }

    public MaitreOeuvrage getClientById(Long id) {
        return maitreOeuvrageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public MaitreOeuvrage createClient(MaitreOeuvrage client) {
        client.setArchived(false);

        // Generate a random client code if not already set
        if (client.getClientCode() == null || client.getClientCode().isEmpty()) {
            client.setClientCode(generateClientCode());
        }

        return maitreOeuvrageRepository.save(client);
    }

    /**
     * Generates a random client code in the format "ab01", "ab02", etc.
     * Uses a combination of random letters and sequential numbers.
     * @return A unique client code
     */
    private String generateClientCode() {
        // Generate two random lowercase letters
        char firstLetter = (char) (random.nextInt(26) + 'a');
        char secondLetter = (char) (random.nextInt(26) + 'a');

        // Get sequential number and format to 2 digits
        int sequentialNum = counter.getAndIncrement();

        // Format to ensure we have 2 digits (01, 02, etc.)
        String numberPart = String.format("%02d", sequentialNum % 100);

        // Combine to format "ab01"
        return "" + firstLetter + secondLetter + numberPart;
    }

    public MaitreOeuvrage updateClient(Long id, MaitreOeuvrage clientDetails) {
        MaitreOeuvrage existingClient = maitreOeuvrageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        existingClient.setName(clientDetails.getName());
        existingClient.setWebSite(clientDetails.getWebSite());
        existingClient.setAddress(clientDetails.getAddress());
        existingClient.setSecteur(clientDetails.getSecteur());
        // Update the new fields
        existingClient.setCountry(clientDetails.getCountry());
        existingClient.setCity(clientDetails.getCity());
        existingClient.setLandline(clientDetails.getLandline());
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
