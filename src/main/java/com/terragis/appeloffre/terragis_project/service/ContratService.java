package com.terragis.appeloffre.terragis_project.service;

import com.terragis.appeloffre.terragis_project.entity.*;
import com.terragis.appeloffre.terragis_project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContratService {
    private final ContratRepository contratRepository;
    private final OffreRepository offreRepository;
    private final LivrableRepository livrableRepository;
    private final PDFGenerationService pdfGenerationService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public List<Contrat> getAllContrats() {
        // Fetch all contracts with eager loading of related entities
        List<Contrat> contrats = contratRepository.findAll();

        // Manually initialize the lazy-loaded collections to avoid LazyInitializationException
        for (Contrat contrat : contrats) {
            if (contrat.getOffre() != null) {
                // Initialize offre's collections
                if (contrat.getOffre().getTaches() != null) {
                    contrat.getOffre().getTaches().size(); // Force initialization
                }
                if (contrat.getOffre().getDocuments() != null) {
                    contrat.getOffre().getDocuments().size(); // Force initialization
                }

                // Initialize opportunite if available
                if (contrat.getOffre().getOpportunite() != null) {
                    // Force initialization of any needed opportunite properties
                    contrat.getOffre().getOpportunite().getProjectName();

                    // Initialize client if available
                    if (contrat.getOffre().getOpportunite().getClient() != null) {
                        contrat.getOffre().getOpportunite().getClient().getName();

                        // Initialize contacts if available
                        if (contrat.getOffre().getOpportunite().getClient().getContacts() != null) {
                            contrat.getOffre().getOpportunite().getClient().getContacts().size();
                        }
                    }
                }
            }

            // Initialize livrables
            if (contrat.getLivrables() != null) {
                contrat.getLivrables().size(); // Force initialization
            }
        }

        return contrats;
    }

    public Optional<Contrat> getContratById(Long id) {
        Optional<Contrat> contratOpt = contratRepository.findById(id);

        // If contract exists, initialize lazy-loaded collections
        contratOpt.ifPresent(contrat -> {
            if (contrat.getOffre() != null) {
                // Initialize offre's collections
                if (contrat.getOffre().getTaches() != null) {
                    contrat.getOffre().getTaches().size(); // Force initialization
                }
                if (contrat.getOffre().getDocuments() != null) {
                    contrat.getOffre().getDocuments().size(); // Force initialization
                }

                // Initialize opportunite if available
                if (contrat.getOffre().getOpportunite() != null) {
                    // Force initialization of any needed opportunite properties
                    contrat.getOffre().getOpportunite().getProjectName();

                    // Initialize client if available
                    if (contrat.getOffre().getOpportunite().getClient() != null) {
                        contrat.getOffre().getOpportunite().getClient().getName();

                        // Initialize contacts if available
                        if (contrat.getOffre().getOpportunite().getClient().getContacts() != null) {
                            contrat.getOffre().getOpportunite().getClient().getContacts().size();
                        }
                    }
                }
            }

            // Initialize livrables
            if (contrat.getLivrables() != null) {
                contrat.getLivrables().size(); // Force initialization
            }
        });

        return contratOpt;
    }

    public Contrat createContrat(Contrat contrat) {
        // Vérifier que l'offre existe
        if (contrat.getOffreId() != null) {
            Offre offre = offreRepository.findById(contrat.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée avec l'ID: " + contrat.getOffreId()));
            contrat.setOffre(offre);
        }

        // Définir le statut par défaut
        if (contrat.getStatut() == null) {
            contrat.setStatut("ACTIF");
        }

        // Définir la date de création
        contrat.setDateCreation(new Date());

        return contratRepository.save(contrat);
    }

    public Contrat updateContrat(Long id, Contrat contratDetails) {
        Contrat existingContrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

        existingContrat.setStartDate(contratDetails.getStartDate());
        existingContrat.setEndDate(contratDetails.getEndDate());
        existingContrat.setDetails(contratDetails.getDetails());
        existingContrat.setNameClient(contratDetails.getNameClient());
        existingContrat.setStatut(contratDetails.getStatut());

        return contratRepository.save(existingContrat);
    }

    public void deleteContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

        // Avant de supprimer le contrat, cassez la relation avec l'offre
        if (contrat.getOffre() != null) {
            Offre offre = contrat.getOffre();
            offre.setContrat(null);
            offreRepository.save(offre);
        }

        // Supprimez d'abord les livrables associés
        livrableRepository.deleteByContratId(id);

        // Puis supprimez le contrat
        contratRepository.delete(contrat);
    }

    public Contrat signContrat(Long id, String signature, String signerName) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

        contrat.setSignature(signature);
        contrat.setSignerName(signerName);
        contrat.setDateSignature(new Date());
        contrat.setSigned(true);

        return contratRepository.save(contrat);
    }

    public Resource generateContratPDF(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

        log.info("Génération du PDF pour le contrat ID: {}", id);
        byte[] pdfBytes = pdfGenerationService.generateContratPDF(contrat);
        log.info("PDF généré avec succès pour le contrat ID: {} ({} bytes)", id, pdfBytes.length);

        return new ByteArrayResource(pdfBytes);
    }

    public Map<String, Object> sendContratByEmail(Long id) {
        try {
            Contrat contrat = contratRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

            log.info("Envoi par email du contrat ID: {}", id);

            // Générer le PDF
            byte[] pdfBytes = pdfGenerationService.generateContratPDF(contrat);

            // Obtenir l'email du client
            String clientEmail = getClientEmail(contrat);
            if (clientEmail == null || clientEmail.isEmpty()) {
                clientEmail = "client@example.com";
                log.warn("Email du client non trouvé pour le contrat {}, utilisation de l'email par défaut: {}", id, clientEmail);
            }

            // Vérifier si le service email est configuré
            if (mailSender == null) {
                throw new RuntimeException("Service email non configuré. Veuillez configurer les paramètres SMTP dans application.properties");
            }

            // Envoyer l'email
            sendContratByEmailInternal(contrat, pdfBytes, clientEmail);

            // Marquer le contrat comme envoyé
            contrat.setDateEnvoi(new Date());
            contratRepository.save(contrat);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Contrat envoyé par email avec succès");
            result.put("emailSent", clientEmail);
            result.put("dateSent", new Date());

            log.info("Email envoyé avec succès à {} pour le contrat {}", clientEmail, id);
            return result;

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi par email du contrat {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi par email: " + e.getMessage());
        }
    }

    public Map<String, Object> generateAndSendContratPDF(Long id) {
        try {
            Contrat contrat = contratRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'ID: " + id));

            log.info("Génération et envoi du PDF pour le contrat ID: {}", id);

            // Générer le PDF
            byte[] pdfBytes = pdfGenerationService.generateContratPDF(contrat);

            // Obtenir l'email du client
            String clientEmail = getClientEmail(contrat);
            if (clientEmail == null || clientEmail.isEmpty()) {
                clientEmail = "client@example.com";
                log.warn("Email du client non trouvé pour le contrat {}, utilisation de l'email par défaut: {}", id, clientEmail);
            }

            log.info("Tentative d'envoi du contrat à l'email: {}", clientEmail);

            // Essayer d'envoyer l'email
            boolean emailSent = false;
            String message = "";

            if (mailSender != null) {
                try {
                    sendContratByEmailInternal(contrat, pdfBytes, clientEmail);
                    emailSent = true;
                    message = "Contrat généré et envoyé par email avec succès";
                    log.info("Email envoyé avec succès à {}", clientEmail);
                } catch (Exception emailError) {
                    log.warn("Erreur lors de l'envoi de l'email: {}. Le PDF a été généré mais pas envoyé.", emailError.getMessage());
                    emailSent = false;
                    message = "Contrat généré avec succès. Email non envoyé (configuration email requise): " + emailError.getMessage();
                }
            } else {
                log.warn("JavaMailSender non configuré. Le PDF a été généré mais pas envoyé.");
                message = "Contrat généré avec succès. Email non envoyé (JavaMailSender non configuré)";
            }

            // Marquer le contrat comme traité
            if (emailSent) {
                contrat.setDateEnvoi(new Date());
                contratRepository.save(contrat);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", message);
            result.put("emailSent", emailSent ? clientEmail : "Non envoyé");
            result.put("dateSent", new Date());
            result.put("pdfGenerated", true);

            return result;

        } catch (Exception e) {
            log.error("Erreur lors de la génération/envoi du contrat {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération/envoi: " + e.getMessage());
        }
    }

    private String getClientEmail(Contrat contrat) {
        try {
            if (contrat.getOffre() != null &&
                    contrat.getOffre().getOpportunite() != null &&
                    contrat.getOffre().getOpportunite().getClient() != null) {
                MaitreOeuvrage client = contrat.getOffre().getOpportunite().getClient();
                // Essayer de récupérer l'email depuis les contacts
                if (client.getContacts() != null && !client.getContacts().isEmpty()) {
                    String email = client.getContacts().get(0).getEmail();
                    if (email != null && !email.trim().isEmpty()) {
                        return email.trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors de la récupération de l'email du client: {}", e.getMessage());
        }
        return null;
    }

    private void sendContratByEmailInternal(Contrat contrat, byte[] pdfBytes, String clientEmail) throws Exception {
        if (mailSender == null) {
            throw new RuntimeException("Service email non configuré");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(clientEmail);
        helper.setFrom("noreply@terragis.com");
        helper.setSubject("Contrat - " + contrat.getNameClient());

        String emailBody = buildEmailBody(contrat);
        helper.setText(emailBody, true);

        // Attacher le PDF
        String fileName = "contrat_" + contrat.getId() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    private String buildEmailBody(Contrat contrat) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
                        <h2 style="color: #007bff; margin-top: 0;">📋 Contrat - %s</h2>
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Veuillez trouver en pièce jointe le contrat signé pour le projet <strong>%s</strong>.</p>
                    </div>
                                    
                    <div style="background-color: #fff; border: 1px solid #dee2e6; border-radius: 8px; padding: 20px; margin-bottom: 20px;">
                        <h3 style="color: #28a745; margin-top: 0;">📋 Détails du contrat :</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li style="padding: 5px 0;"><strong>📅 Date de début :</strong> %s</li>
                            <li style="padding: 5px 0;"><strong>📅 Date de fin :</strong> %s</li>
                            <li style="padding: 5px 0;"><strong>💰 Budget :</strong> %s MAD</li>
                            <li style="padding: 5px 0;"><strong>📄 Statut :</strong> %s</li>
                        </ul>
                    </div>
                                    
                    <div style="background-color: #e9ecef; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
                        <p style="margin: 0;"><strong>📝 Détails :</strong></p>
                        <p style="margin: 10px 0 0 0;">%s</p>
                    </div>
                                    
                    <div style="background-color: #007bff; color: white; padding: 15px; border-radius: 8px; text-align: center;">
                        <p style="margin: 0;">Cordialement,<br/>
                        <strong>L'équipe Terragis</strong></p>
                        <p style="margin: 10px 0 0 0; font-size: 12px; opacity: 0.8;">
                            Ce document a été généré automatiquement le %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                contrat.getNameClient(),
                contrat.getNameClient(),
                contrat.getOffre() != null && contrat.getOffre().getOpportunite() != null ?
                        contrat.getOffre().getOpportunite().getProjectName() : "N/A",
                contrat.getStartDate() != null ? contrat.getStartDate().toString() : "N/A",
                contrat.getEndDate() != null ? contrat.getEndDate().toString() : "N/A",
                contrat.getOffre() != null ? contrat.getOffre().getBudget() : "N/A",
                contrat.getStatut() != null ? contrat.getStatut() : "ACTIF",
                contrat.getDetails() != null ? contrat.getDetails() : "Aucun détail spécifié",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
        );
    }

    public List<Livrable> getContratLivrables(Long contratId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        return contrat.getLivrables() != null ? contrat.getLivrables() : new ArrayList<>();
    }

    public Livrable addLivrable(Long contratId, Livrable livrable) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));

        livrable.setContrat(contrat);
        if (livrable.getStatutValidation() == null) {
            livrable.setStatutValidation(StatutValidation.EN_ATTENTE);
        }
        if (livrable.getStatutPaiement() == null) {
            livrable.setStatutPaiement(StatutPaiement.NON_PAYE);
        }

        return livrableRepository.save(livrable);
    }

    public Livrable updateLivrable(Long contratId, Long livrableId, Livrable livrableDetails) {
        Livrable existingLivrable = livrableRepository.findById(livrableId)
                .orElseThrow(() -> new RuntimeException("Livrable non trouvé"));

        if (!existingLivrable.getContrat().getId().equals(contratId)) {
            throw new RuntimeException("Le livrable n'appartient pas à ce contrat");
        }

        existingLivrable.setTitre(livrableDetails.getTitre());
        existingLivrable.setDescription(livrableDetails.getDescription());
        existingLivrable.setDateLivraison(livrableDetails.getDateLivraison());
        existingLivrable.setMontant(livrableDetails.getMontant());
        existingLivrable.setStatutValidation(livrableDetails.getStatutValidation());
        existingLivrable.setStatutPaiement(livrableDetails.getStatutPaiement());
        existingLivrable.setFichierJoint(livrableDetails.getFichierJoint());

        return livrableRepository.save(existingLivrable);
    }

    public void deleteLivrable(Long contratId, Long livrableId) {
        Livrable livrable = livrableRepository.findById(livrableId)
                .orElseThrow(() -> new RuntimeException("Livrable non trouvé"));

        if (!livrable.getContrat().getId().equals(contratId)) {
            throw new RuntimeException("Le livrable n'appartient pas à ce contrat");
        }

        livrableRepository.delete(livrable);
    }
}