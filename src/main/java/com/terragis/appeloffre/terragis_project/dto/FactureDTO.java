package com.terragis.appeloffre.terragis_project.dto;

import com.terragis.appeloffre.terragis_project.entity.StatutPaiement;
import lombok.Data;

import java.util.Date;

@Data
public class FactureDTO {
    private Long id;
    private String numeroFacture;
    private Date dateFacture;
    private Double montantTotal;
    private StatutPaiement statutFacture;
    private Long contratId;
    private String clientName;
    private String contratDetails;

    // Champs calculés
    private boolean enRetard;
    private int joursRetard;
    private Double montantRestant;
}
