package com.croise.covoiturage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrajetRequestDTO(

        @NotNull(message = "L'id du conducteur est obligatoire")
        Long conducteurId,

        @NotNull(message = "L'id du véhicule est obligatoire")
        Long vehiculeId,

        @Min(value = 1, message = "Il faut proposer au moins 1 place")
        int placesProposees,

        @NotEmpty(message = "Le parcours doit contenir au moins 2 points d'arrêt")
        @Valid
        List<PointArretRequestDTO> pointsArret
) {}