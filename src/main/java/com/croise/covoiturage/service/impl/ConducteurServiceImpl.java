package com.croise.covoiturage.service.impl;

import com.croise.covoiturage.dto.ConducteurRequestDTO;
import com.croise.covoiturage.dto.ConducteurResponseDTO;
import com.croise.covoiturage.entity.Conducteur;
import com.croise.covoiturage.exception.RegleMetierException;
import com.croise.covoiturage.exception.RessourceNotFoundException;
import com.croise.covoiturage.mapper.ConducteurMapper;
import com.croise.covoiturage.repository.ConducteurRepository;
import com.croise.covoiturage.service.ConducteurService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConducteurServiceImpl implements ConducteurService {

    private static final Logger log = LoggerFactory.getLogger(ConducteurServiceImpl.class);

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    public ConducteurResponseDTO creer(ConducteurRequestDTO dto) {

        if (conducteurRepository.existsByEmail(dto.email())) {
            log.warn("Tentative de création avec un email déjà utilisé : {}", dto.email());
            throw new RegleMetierException("Cet email est déjà utilisé : " + dto.email());
        }

        if (conducteurRepository.existsByNumeroPermis(dto.numeroPermis())) {
            log.warn("Tentative de création avec un numéro de permis déjà utilisé : {}", dto.numeroPermis());
            throw new RegleMetierException("Ce numéro de permis est déjà utilisé : " + dto.numeroPermis());
        }

        Conducteur conducteur = conducteurMapper.toEntity(dto);
        Conducteur sauvegarde = conducteurRepository.save(conducteur);

        log.info("Conducteur créé avec l'id {}", sauvegarde.getId());

        return conducteurMapper.toResponseDTO(sauvegarde);
    }

    @Override
    public ConducteurResponseDTO consulter(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Conducteur introuvable avec l'id : " + id));
        return conducteurMapper.toResponseDTO(conducteur);
    }

    @Override
    public List<ConducteurResponseDTO> listerTous() {
        return conducteurRepository.findAll()
                .stream()
                .map(conducteurMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ConducteurResponseDTO modifier(Long id, ConducteurRequestDTO dto) {

        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Conducteur introuvable avec l'id : " + id));

        // Vérifier l'unicité de l'email seulement si l'email a changé
        if (!conducteur.getEmail().equals(dto.email())
                && conducteurRepository.existsByEmail(dto.email())) {
            throw new RegleMetierException("Cet email est déjà utilisé : " + dto.email());
        }

        // Vérifier l'unicité du permis seulement s'il a changé
        if (!conducteur.getNumeroPermis().equals(dto.numeroPermis())
                && conducteurRepository.existsByNumeroPermis(dto.numeroPermis())) {
            throw new RegleMetierException("Ce numéro de permis est déjà utilisé : " + dto.numeroPermis());
        }

        conducteur.setNom(dto.nom());
        conducteur.setPrenom(dto.prenom());
        conducteur.setEmail(dto.email());
        conducteur.setTelephone(dto.telephone());
        conducteur.setNumeroPermis(dto.numeroPermis());

        Conducteur miseAJour = conducteurRepository.save(conducteur);

        log.info("Conducteur modifié avec l'id {}", id);

        return conducteurMapper.toResponseDTO(miseAJour);
    }

    @Override
    public List<ConducteurResponseDTO> rechercher(String motCle) {
        return conducteurRepository
                .findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(motCle, motCle)
                .stream()
                .map(conducteurMapper::toResponseDTO)
                .toList();
    }
}