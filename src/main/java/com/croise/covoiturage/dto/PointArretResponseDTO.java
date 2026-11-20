package com.croise.covoiturage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretResponseDTO(
        Long id,
        String ville,
        int ordre,
        LocalDateTime heurePassage,
        BigDecimal prixDepuisDepart
) {}