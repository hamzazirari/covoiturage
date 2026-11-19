package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.PassagerRequestDTO;
import com.croise.covoiturage.dto.PassagerResponseDTO;

import java.util.List;

public interface PassagerService {

    PassagerResponseDTO creer(PassagerRequestDTO dto);

    PassagerResponseDTO consulter(Long id);

    List<PassagerResponseDTO> listerTous();

    PassagerResponseDTO modifier(Long id, PassagerRequestDTO dto);

    List<PassagerResponseDTO> rechercherParEmail(String email);
}