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

        if (client.getClientCode() == null || client.getClientCode().isEmpty()) {
            client.setClientCode(generateAlphabeticalClientCode());
        }

        return maitreOeuvrageRepository.save(client);
    }

    /**
     * Generates an alphabetical sequential client code in the format "a01", "a02", ..., "a99", "b01", "b02", etc.
     * @return A unique alphabetical client code
     */
    private String generateAlphabeticalClientCode() {
        // Find the highest existing code
        List<MaitreOeuvrage> allClients = maitreOeuvrageRepository.findAll();

        char maxLetter = 'a';
        int maxNumber = 0;

        for (MaitreOeuvrage existingClient : allClients) {
            String existingCode = existingClient.getClientCode();
            if (existingCode != null && existingCode.length() == 3 &&
                    Character.isLetter(existingCode.charAt(0)) &&
                    Character.isDigit(existingCode.charAt(1)) &&
                    Character.isDigit(existingCode.charAt(2))) {

                char letter = existingCode.charAt(0);
                int number = Integer.parseInt(existingCode.substring(1));

                if (letter > maxLetter || (letter == maxLetter && number > maxNumber)) {
                    maxLetter = letter;
                    maxNumber = number;
                }
            }
        }

        // Generate next code
        if (maxNumber >= 99) {
            // Move to next letter and reset to 01
            maxLetter = (char) (maxLetter + 1);
            maxNumber = 1;
        } else {
            maxNumber++;
        }

        // Format as letter + 2-digit number (a01, a02, etc.)
        return String.format("%c%02d", maxLetter, maxNumber);
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
