package com.croise.covoiturage.dto;

import com.croise.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponseDTO(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        int placesProposees,
        int placesRestantes,
        StatutTrajet statut,
        Long conducteurId,
        String nomConducteur,
        Long vehiculeId,
        List<PointArretResponseDTO> pointsArret
) {}