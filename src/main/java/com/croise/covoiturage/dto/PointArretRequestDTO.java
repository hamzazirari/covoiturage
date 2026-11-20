package com.croise.covoiturage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretRequestDTO(

        @NotBlank(message = "La ville est obligatoire")
        String ville,

        @NotNull(message = "L'heure de passage est obligatoire")
        LocalDateTime heurePassage,

        @NotNull(message = "Le prix depuis le départ est obligatoire")
        BigDecimal prixDepuisDepart
) {}