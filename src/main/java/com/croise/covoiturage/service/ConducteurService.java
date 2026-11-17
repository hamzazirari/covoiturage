package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.ConducteurRequestDTO;
import com.croise.covoiturage.dto.ConducteurResponseDTO;

import java.util.List;

public interface ConducteurService {

    ConducteurResponseDTO creer(ConducteurRequestDTO dto);

    ConducteurResponseDTO consulter(Long id);

    List<ConducteurResponseDTO> listerTous();

    ConducteurResponseDTO modifier(Long id, ConducteurRequestDTO dto);

    List<ConducteurResponseDTO> rechercher(String motCle);
}