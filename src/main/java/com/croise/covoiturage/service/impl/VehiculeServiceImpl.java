package com.croise.covoiturage.service.impl;

import com.croise.covoiturage.dto.VehiculeRequestDTO;
import com.croise.covoiturage.dto.VehiculeResponseDTO;
import com.croise.covoiturage.entity.Conducteur;
import com.croise.covoiturage.entity.Vehicule;
import com.croise.covoiturage.exception.RegleMetierException;
import com.croise.covoiturage.exception.RessourceNotFoundException;
import com.croise.covoiturage.mapper.VehiculeMapper;
import com.croise.covoiturage.repository.ConducteurRepository;
import com.croise.covoiturage.repository.VehiculeRepository;
import com.croise.covoiturage.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private static final Logger log = LoggerFactory.getLogger(VehiculeServiceImpl.class);

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    public VehiculeResponseDTO ajouter(VehiculeRequestDTO dto) {

        Conducteur conducteur = conducteurRepository.findById(dto.conducteurId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Conducteur introuvable avec l'id : " + dto.conducteurId()));

        if (vehiculeRepository.existsByImmatriculation(dto.immatriculation())) {
            log.warn("Tentative d'ajout avec une immatriculation déjà utilisée : {}", dto.immatriculation());
            throw new RegleMetierException("Cette immatriculation est déjà utilisée : " + dto.immatriculation());
        }

        Vehicule vehicule = vehiculeMapper.toEntity(dto);
        vehicule.setConducteur(conducteur);

        Vehicule sauvegarde = vehiculeRepository.save(vehicule);

        log.info("Véhicule créé avec l'id {} pour le conducteur {}", sauvegarde.getId(), conducteur.getId());

        return vehiculeMapper.toResponseDTO(sauvegarde);
    }

    @Override
    public VehiculeResponseDTO consulter(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Véhicule introuvable avec l'id : " + id));
        return vehiculeMapper.toResponseDTO(vehicule);
    }

    @Override
    public VehiculeResponseDTO modifier(Long id, VehiculeRequestDTO dto) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Véhicule introuvable avec l'id : " + id));

        if (!vehicule.getImmatriculation().equals(dto.immatriculation())
                && vehiculeRepository.existsByImmatriculation(dto.immatriculation())) {
            throw new RegleMetierException("Cette immatriculation est déjà utilisée : " + dto.immatriculation());
        }

        // if le conducteur change f la requete  on veerifi quil existe
        if (!vehicule.getConducteur().getId().equals(dto.conducteurId())) {
            Conducteur nouveauConducteur = conducteurRepository.findById(dto.conducteurId())
                    .orElseThrow(() -> new RessourceNotFoundException(
                            "Conducteur introuvable avec l'id : " + dto.conducteurId()));
            vehicule.setConducteur(nouveauConducteur);
        }

        vehicule.setMarque(dto.marque());
        vehicule.setModele(dto.modele());
        vehicule.setImmatriculation(dto.immatriculation());
        vehicule.setCouleur(dto.couleur());
        vehicule.setCapacite(dto.capacite());

        Vehicule miseAJour = vehiculeRepository.save(vehicule);

        log.info("Véhicule modifié avec l'id {}", id);

        return vehiculeMapper.toResponseDTO(miseAJour);
    }

    @Override
    public List<VehiculeResponseDTO> listerParConducteur(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new RessourceNotFoundException("Conducteur introuvable avec l'id : " + conducteurId);
        }

        return vehiculeRepository.findByConducteurId(conducteurId)
                .stream()
                .map(vehiculeMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void supprimer(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Véhicule introuvable avec l'id : " + id));

        if (vehiculeRepository.estAssocieAUnTrajet(vehicule.getId())) {
            log.warn("Tentative de suppression d'un véhicule associé à un trajet, id {}", id);
            throw new RegleMetierException("Impossible de supprimer ce véhicule : il est associé à au moins un trajet");
        }

        vehiculeRepository.delete(vehicule);

        log.info("Véhicule supprimé avec l'id {}", id);
    }
}