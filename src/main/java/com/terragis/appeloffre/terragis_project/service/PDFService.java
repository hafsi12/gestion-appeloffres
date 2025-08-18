package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.Facture;
import com.terragis.appeloffre.terragis_project.entity.Livrable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PDFService {

    public byte[] genererTicketPDF(Facture facture) throws Exception {
        StringBuilder ticket = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // En-tête du ticket
        ticket.append("═══════════════════════════════════════\n");
        ticket.append("           TICKET DE PAIEMENT           \n");
        ticket.append("═══════════════════════════════════════\n\n");

        // Informations de la facture
        ticket.append("Numéro de Facture: ").append(facture.getNumeroFacture()).append("\n");
        ticket.append("Date d'émission: ").append(sdf.format(facture.getDateFacture())).append("\n");
        ticket.append("Client: ").append(facture.getContrat().getNameClient() != null ?
                facture.getContrat().getNameClient() : "Client").append("\n");
        ticket.append("Contrat ID: ").append(facture.getContrat().getId()).append("\n\n");

        // Statut de paiement
        ticket.append("    ✓ FACTURE PAYÉE AVEC SUCCÈS ✓    \n\n");

        // Détail des livrables
        ticket.append("DÉTAIL DES LIVRABLES:\n");
        ticket.append("───────────────────────────────────────\n");
        ticket.append(String.format("%-20s %-12s %-12s %-10s\n",
                "Description", "Date", "Montant", "Statut"));
        ticket.append("───────────────────────────────────────\n");

        double totalMontant = 0;
        if (facture.getContrat().getLivrables() != null) {
            for (Livrable livrable : facture.getContrat().getLivrables()) {
                String description = livrable.getDescription() != null ?
                        (livrable.getDescription().length() > 18 ?
                                livrable.getDescription().substring(0, 15) + "..." :
                                livrable.getDescription()) : "N/A";
                String dateLivraison = livrable.getDateLivraison() != null ?
                        sdf.format(livrable.getDateLivraison()).substring(0, 10) : "N/A";
                double montant = livrable.getMontant() != null ? livrable.getMontant() : 0.0;

                ticket.append(String.format("%-20s %-12s %8.2f MAD %-10s\n",
                        description, dateLivraison, montant, "Payé"));
                totalMontant += montant;
            }
        }

        ticket.append("───────────────────────────────────────\n");
        ticket.append(String.format("MONTANT TOTAL: %15.2f MAD\n", totalMontant));
        ticket.append("───────────────────────────────────────\n\n");

        // Message de confirmation
        ticket.append("Paiement effectué avec succès le ").append(sdf.format(new Date())).append("\n\n");
        ticket.append("        Merci pour votre confiance!        \n");
        ticket.append("═══════════════════════════════════════\n");

        return ticket.toString().getBytes(StandardCharsets.UTF_8);
    }
}
