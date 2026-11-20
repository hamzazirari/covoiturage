package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;

public interface TrajetService {

    TrajetResponseDTO publier(TrajetRequestDTO dto);

    TrajetResponseDTO consulterDetail(Long id);
}