package com.croise.covoiturage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculeRequestDTO(

        @NotNull(message = "L'id du conducteur est obligatoire")
        Long conducteurId,

        @NotBlank(message = "La marque est obligatoire")
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        String modele,

        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,

        @NotBlank(message = "La couleur est obligatoire")
        String couleur,

        @Min(value = 1, message = "La capacité minimale est de 1 place")
        @Max(value = 8, message = "La capacité maximale est de 8 places")
        int capacite
) {}