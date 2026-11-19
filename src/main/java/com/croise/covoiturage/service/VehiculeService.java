package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.VehiculeRequestDTO;
import com.croise.covoiturage.dto.VehiculeResponseDTO;

import java.util.List;

public interface VehiculeService {

    VehiculeResponseDTO ajouter(VehiculeRequestDTO dto);

    VehiculeResponseDTO consulter(Long id);

    VehiculeResponseDTO modifier(Long id, VehiculeRequestDTO dto);

    List<VehiculeResponseDTO> listerParConducteur(Long conducteurId);

    void supprimer(Long id);
}