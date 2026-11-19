package com.croise.covoiturage.service.impl;

import com.croise.covoiturage.dto.PassagerRequestDTO;
import com.croise.covoiturage.dto.PassagerResponseDTO;
import com.croise.covoiturage.entity.Passager;
import com.croise.covoiturage.exception.RegleMetierException;
import com.croise.covoiturage.exception.RessourceNotFoundException;
import com.croise.covoiturage.mapper.PassagerMapper;
import com.croise.covoiturage.repository.PassagerRepository;
import com.croise.covoiturage.service.PassagerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log = LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;

    @Override
    public PassagerResponseDTO creer(PassagerRequestDTO dto) {

        if (passagerRepository.existsByEmail(dto.email())) {
            log.warn("Tentative de création avec un email déjà utilisé : {}", dto.email());
            throw new RegleMetierException("Cet email est déjà utilisé : " + dto.email());
        }

        Passager passager = passagerMapper.toEntity(dto);
        Passager sauvegarde = passagerRepository.save(passager);

        log.info("Passager créé avec l'id {}", sauvegarde.getId());

        return passagerMapper.toResponseDTO(sauvegarde);
    }

    @Override
    public PassagerResponseDTO consulter(Long id) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Passager introuvable avec l'id : " + id));
        return passagerMapper.toResponseDTO(passager);
    }

    @Override
    public List<PassagerResponseDTO> listerTous() {
        return passagerRepository.findAll()
                .stream()
                .map(passagerMapper::toResponseDTO)
                .toList();
    }

    @Override
    public PassagerResponseDTO modifier(Long id, PassagerRequestDTO dto) {

        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Passager introuvable avec l'id : " + id));

        if (!passager.getEmail().equals(dto.email())
                && passagerRepository.existsByEmail(dto.email())) {
            throw new RegleMetierException("Cet email est déjà utilisé : " + dto.email());
        }

        passager.setNom(dto.nom());
        passager.setPrenom(dto.prenom());
        passager.setEmail(dto.email());
        passager.setTelephone(dto.telephone());

        Passager miseAJour = passagerRepository.save(passager);

        log.info("Passager modifié avec l'id {}", id);

        return passagerMapper.toResponseDTO(miseAJour);
    }

    @Override
    public List<PassagerResponseDTO> rechercherParEmail(String email) {
        return passagerRepository.findByEmailContainingIgnoreCase(email)
                .stream()
                .map(passagerMapper::toResponseDTO)
                .toList();
    }
}