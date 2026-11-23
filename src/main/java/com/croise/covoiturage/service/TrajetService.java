package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.TrajetRechercheResponseDTO;
import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.repository.ReservationRepository;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrajetService {

    TrajetResponseDTO publier(TrajetRequestDTO dto);

    TrajetResponseDTO consulterDetail(Long id);

    Page<TrajetRechercheResponseDTO> rechercher(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            int placesDemandees,
            BigDecimal prixMax,
            int page,
            int taille
    );

    void terminer(Long trajetId, Long conducteurId);

    void annulerTrajet(Long trajetId, Long conducteurId);
}