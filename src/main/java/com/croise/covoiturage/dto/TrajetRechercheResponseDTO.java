package com.croise.covoiturage.dto;

import com.croise.covoiturage.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetRechercheResponseDTO(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        BigDecimal prixSegment,
        Long placesRestantes,
        StatutTrajet statut
) {}