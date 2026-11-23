package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.ReservationRequestDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;

import java.util.List;

public interface ReservationService {

    ReservationResponseDTO reserver(ReservationRequestDTO dto);

    void annuler(Long reservationId, Long passagerId);

    List<ReservationResponseDTO> consulterParPassager(Long passagerId);

    List<ReservationResponseDTO> consulterParTrajet(Long trajetId, Long conducteurId);
}