package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.Contrat;
import com.terragis.appeloffre.terragis_project.entity.Livrable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;

@Service
public class PDFGenerationService {

    public byte[] generateContratPDF(Contrat contrat) {
        try {
            // Pour cette version simplifiée, nous générons un HTML qui peut être converti en PDF
            // par le navigateur ou par un service externe
            String htmlContent = generateHTMLContent(contrat);

            // En production, vous pouvez utiliser:
            // - Puppeteer via une API REST
            // - wkhtmltopdf via ProcessBuilder
            // - Une bibliothèque comme Flying Saucer

            // Pour cette démonstration, nous retournons le HTML en tant que "PDF"
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(htmlContent.getBytes("UTF-8"));

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    private String generateHTMLContent(Contrat contrat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Contrat - ").append(contrat.getNameClient()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }");
        html.append(".section { margin: 20px 0; }");
        html.append(".section h3 { color: #333; border-bottom: 1px solid #ccc; padding-bottom: 5px; }");
        html.append(".info-table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append(".info-table td { padding: 8px; border: 1px solid #ddd; }");
        html.append(".info-table td:first-child { background-color: #f5f5f5; font-weight: bold; width: 30%; }");
        html.append(".signature-section { margin-top: 50px; border: 2px solid #333; padding: 20px; }");
        html.append(".signature-box { border: 1px solid #000; height: 100px; margin: 10px 0; padding: 10px; }");
        html.append(".livrable-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(".livrable-table th, .livrable-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append(".livrable-table th { background-color: #f2f2f2; font-weight: bold; }");
        html.append(".badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; }");
        html.append(".badge-success { background-color: #d4edda; color: #155724; }");
        html.append(".badge-warning { background-color: #fff3cd; color: #856404; }");
        html.append(".badge-danger { background-color: #f8d7da; color: #721c24; }");
        html.append("@media print { body { margin: 20px; } }");
        html.append("</style>");
        html.append("</head><body>");

        // En-tête
        html.append("<div class='header'>");
        html.append("<h1>CONTRAT DE MARCHÉ</h1>");
        html.append("<h2>N° ").append(contrat.getId()).append("</h2>");
        html.append("<p><strong>Date de génération:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("</p>");
        html.append("</div>");

        // Client (Maître d'ouvrage)
        html.append("<div class='section'>");
        html.append("<h3>CLIENT (MAÎTRE D'OUVRAGE)</h3>");
        html.append("<table class='info-table'>");

        // Get client information from the offer's opportunity
        String clientName = contrat.getNameClient();
        String clientSector = "";
        String clientAddress = "";
        String clientWebsite = "";

        if (contrat.getOffre() != null &&
                contrat.getOffre().getOpportunite() != null &&
                contrat.getOffre().getOpportunite().getClient() != null) {

            clientName = contrat.getOffre().getOpportunite().getClient().getName();
            clientSector = contrat.getOffre().getOpportunite().getClient().getSecteur();
            clientAddress = contrat.getOffre().getOpportunite().getClient().getAddress();
            clientWebsite = contrat.getOffre().getOpportunite().getClient().getWebSite();
        }

        html.append("<tr><td>Nom du client</td><td>").append(clientName).append("</td></tr>");
        html.append("<tr><td>Secteur</td><td>").append(clientSector != null ? clientSector : "N/A").append("</td></tr>");
        html.append("<tr><td>Adresse</td><td>").append(clientAddress != null ? clientAddress : "N/A").append("</td></tr>");
        html.append("<tr><td>Site web</td><td>").append(clientWebsite != null ? clientWebsite : "N/A").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Opportunité
        html.append("<div class='section'>");
        html.append("<h3>OPPORTUNITÉ</h3>");
        html.append("<table class='info-table'>");

        String projectName = "N/A";
        String projectDescription = "N/A";
        String projectDeadline = "N/A";

        if (contrat.getOffre() != null && contrat.getOffre().getOpportunite() != null) {
            projectName = contrat.getOffre().getOpportunite().getProjectName();
            projectDescription = contrat.getOffre().getOpportunite().getDescription();
            if (contrat.getOffre().getOpportunite().getDeadline() != null) {
                projectDeadline = dateFormat.format(contrat.getOffre().getOpportunite().getDeadline());
            }
        }

        html.append("<tr><td>Titre du projet</td><td>").append(projectName).append("</td></tr>");
        html.append("<tr><td>Date limite</td><td>").append(projectDeadline).append("</td></tr>");
        html.append("<tr><td>Description du projet</td><td>").append(projectDescription).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Offre
        html.append("<div class='section'>");
        html.append("<h3>OFFRE</h3>");
        html.append("<table class='info-table'>");

        String budget = "N/A";
        String adjuge = "N/A";
        String sent = "Non envoyée";

        if (contrat.getOffre() != null) {
            budget = String.valueOf(contrat.getOffre().getBudget()) + " MAD";
            adjuge = contrat.getOffre().getAdjuge() != null ? contrat.getOffre().getAdjuge().toString() : "N/A";
            sent = contrat.getOffre().isSent() ? "Envoyée" : "Non envoyée";
        }

        html.append("<tr><td>Budget proposé</td><td>").append(budget).append("</td></tr>");
        html.append("<tr><td>Adjugé</td><td>").append(adjuge).append("</td></tr>");
        html.append("<tr><td>Statut d'envoi</td><td>").append(sent).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Contrat
        html.append("<div class='section'>");
        html.append("<h3>CONTRAT</h3>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Date de début</td><td>").append(contrat.getStartDate() != null ? dateFormat.format(contrat.getStartDate()) : "N/A").append("</td></tr>");
        html.append("<tr><td>Date de fin</td><td>").append(contrat.getEndDate() != null ? dateFormat.format(contrat.getEndDate()) : "N/A").append("</td></tr>");
        html.append("<tr><td>Statut</td><td>").append(contrat.getStatut() != null ? contrat.getStatut() : "ACTIF").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Détails du contrat
        html.append("<div class='section'>");
        html.append("<h3>DÉTAILS DU CONTRAT</h3>");
        html.append("<p>").append(contrat.getDetails() != null ? contrat.getDetails() : "Aucun détail spécifié.").append("</p>");
        html.append("</div>");

        // Livrables
        if (contrat.getLivrables() != null && !contrat.getLivrables().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h3>LIVRABLES (").append(contrat.getLivrables().size()).append(")</h3>");
            html.append("<table class='livrable-table'>");
            html.append("<tr>");
            html.append("<th>Titre</th>");
            html.append("<th>Description</th>");
            html.append("<th>Date de livraison</th>");
            html.append("<th>Montant</th>");
            html.append("<th>Statut Validation</th>");
            html.append("<th>Statut Paiement</th>");
            html.append("</tr>");

            for (Livrable livrable : contrat.getLivrables()) {
                html.append("<tr>");
                html.append("<td>").append(livrable.getTitre() != null ? livrable.getTitre() : "").append("</td>");
                html.append("<td>").append(livrable.getDescription() != null ? livrable.getDescription() : "").append("</td>");
                html.append("<td>").append(livrable.getDateLivraison() != null ? dateFormat.format(livrable.getDateLivraison()) : "").append("</td>");
                html.append("<td>").append(livrable.getMontant() != null ? livrable.getMontant() + " MAD" : "").append("</td>");

                // Statut validation avec badge
                String statutValidation = livrable.getStatutValidation() != null ? livrable.getStatutValidation().toString() : "EN_ATTENTE";
                String badgeClass = getBadgeClass(statutValidation);
                html.append("<td><span class='badge ").append(badgeClass).append("'>").append(statutValidation).append("</span></td>");

                // Statut paiement avec badge
                String statutPaiement = livrable.getStatutPaiement() != null ? livrable.getStatutPaiement().toString() : "NON_PAYE";
                String badgeClassPaiement = getBadgeClassPaiement(statutPaiement);
                html.append("<td><span class='badge ").append(badgeClassPaiement).append("'>").append(statutPaiement).append("</span></td>");

                html.append("</tr>");
            }
            html.append("</table>");
            html.append("</div>");
        }

        // Section signature
        html.append("<div class='signature-section'>");
        html.append("<h3>SIGNATURES</h3>");

        if (contrat.isSigned() && contrat.getSignature() != null) {
            html.append("<div style='margin-bottom: 20px;'>");
            html.append("<p><strong>Signé par:</strong> ").append(contrat.getSignerName()).append("</p>");
            html.append("<p><strong>Date de signature:</strong> ").append(contrat.getDateSignature() != null ? dateFormat.format(contrat.getDateSignature()) : "N/A").append("</p>");
            html.append("</div>");

            html.append("<div class='signature-box' style='text-align: center;'>");
            html.append("<p style='margin: 0; color: #666;'>✓ Contrat signé électroniquement</p>");
            html.append("<p style='margin: 5px 0 0 0; font-size: 12px; color: #999;'>Signature vérifiée et authentifiée</p>");
            html.append("</div>");
        } else {
            html.append("<div class='signature-box'>");
            html.append("<p style='text-align: center; margin-top: 40px; color: #666;'>Zone de signature</p>");
            html.append("</div>");
        }

        html.append("<div style='margin-top: 20px; font-size: 12px; color: #666;'>");
        html.append("<p>En signant ce contrat, les parties acceptent les termes et conditions énoncés ci-dessus.</p>");
        html.append("<p>Ce document a été généré électroniquement le ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append(".</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    private String getBadgeClass(String statut) {
        switch (statut) {
            case "VALIDE":
                return "badge-success";
            case "REFUSE":
                return "badge-danger";
            default:
                return "badge-warning";
        }
    }

    private String getBadgeClassPaiement(String statut) {
        switch (statut) {
            case "PAYE":
                return "badge-success";
            case "SOLDE":
                return "badge-success";
            default:
                return "badge-warning";
        }
    }
}
