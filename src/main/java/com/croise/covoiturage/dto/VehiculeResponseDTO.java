package com.croise.covoiturage.dto;

public record VehiculeResponseDTO(
        Long id,
        String marque,
        String modele,
        String immatriculation,
        String couleur,
        int capacite,
        Long conducteurId
) {}