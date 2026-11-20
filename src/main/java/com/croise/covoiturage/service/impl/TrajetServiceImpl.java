package com.croise.covoiturage.service.impl;

import com.croise.covoiturage.dto.PointArretRequestDTO;
import com.croise.covoiturage.dto.TrajetRechercheResponseDTO;
import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.entity.Conducteur;
import com.croise.covoiturage.entity.PointArret;
import com.croise.covoiturage.entity.Trajet;
import com.croise.covoiturage.entity.Vehicule;
import com.croise.covoiturage.enums.StatutTrajet;
import com.croise.covoiturage.exception.AccesInterditException;
import com.croise.covoiturage.exception.RegleMetierException;
import com.croise.covoiturage.exception.RessourceNotFoundException;
import com.croise.covoiturage.mapper.PointArretMapper;
import com.croise.covoiturage.mapper.TrajetMapper;
import com.croise.covoiturage.repository.ConducteurRepository;
import com.croise.covoiturage.repository.TrajetRepository;
import com.croise.covoiturage.repository.VehiculeRepository;
import com.croise.covoiturage.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.croise.covoiturage.dto.TrajetRechercheResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {

    private static final Logger log = LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper trajetMapper;
    private final PointArretMapper pointArretMapper;

    @Override
    public TrajetResponseDTO publier(TrajetRequestDTO dto) {

        //  1  verif conducteur existe
        Conducteur conducteur = conducteurRepository.findById(dto.conducteurId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Conducteur introuvable avec l'id : " + dto.conducteurId()));

        //  2  verif vhicule existe
        Vehicule vehicule = vehiculeRepository.findById(dto.vehiculeId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Véhicule introuvable avec l'id : " + dto.vehiculeId()));

        // 3 le conducteur utilise que ses vehicules
        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            log.warn("Le conducteur {} a tenté d'utiliser le véhicule {} qui ne lui appartient pas",
                    conducteur.getId(), vehicule.getId());
            throw new AccesInterditException("Ce véhicule n'appartient pas à ce conducteur");
        }

        // 4 au moins 2 points
        List<PointArretRequestDTO> points = dto.pointsArret();
        if (points.size() < 2) {
            throw new RegleMetierException("Un trajet doit contenir au moins 2 points d'arrêt");
        }

        // 5 places proposee entre 1 et la capacite de vehicule
        if (dto.placesProposees() > vehicule.getCapacite()) {
            log.warn("Places proposées ({}) supérieures à la capacité du véhicule ({})",
                    dto.placesProposees(), vehicule.getCapacite());
            throw new RegleMetierException(
                    "Le nombre de places proposées ne peut pas dépasser la capacité du véhicule : "
                            + vehicule.getCapacite() + " places au maximum");
        }

        // 6 le depart est dans le futur
        LocalDateTime heureDepart = points.get(0).heurePassage();
        if (heureDepart.isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("La date de départ doit être dans le futur");
        }

        //  7  heures strictement croissantes + prix strictement croissant + premier prix = 0
        BigDecimal premierPrix = points.get(0).prixDepuisDepart();
        if (premierPrix.compareTo(BigDecimal.ZERO) != 0) {
            throw new RegleMetierException("Le prix du premier point (départ) doit être égal à 0");
        }

        for (int i = 1; i < points.size(); i++) {
            LocalDateTime heurePrecedente = points.get(i - 1).heurePassage();
            LocalDateTime heureActuelle = points.get(i).heurePassage();
            if (!heureActuelle.isAfter(heurePrecedente)) {
                throw new RegleMetierException(
                        "Les heures de passage doivent être strictement croissantes");
            }

            BigDecimal prixPrecedent = points.get(i - 1).prixDepuisDepart();
            BigDecimal prixActuel = points.get(i).prixDepuisDepart();
            if (prixActuel.compareTo(prixPrecedent) <= 0) {
                throw new RegleMetierException(
                        "Le prix depuis le départ doit être strictement croissant");
            }
        }

        // 8  une ville napparait qu'une seule fois
        Set<String> villesVues = new HashSet<>();
        for (PointArretRequestDTO point : points) {
            if (!villesVues.add(point.ville().toLowerCase())) {
                throw new RegleMetierException("La ville \"" + point.ville() + "\" apparaît plusieurs fois dans le parcours");
            }
        }

        // 9 Construction  Trajet
        Trajet trajet = new Trajet();
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setPlacesProposees(dto.placesProposees());
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(heureDepart);
        trajet.setVilleDepart(points.get(0).ville());
        trajet.setVilleArrivee(points.get(points.size() - 1).ville());

        List<PointArret> pointsArretEntites = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            PointArret pointArret = pointArretMapper.toEntity(points.get(i));
            pointArret.setOrdre(i);
            pointArret.setTrajet(trajet);
            pointsArretEntites.add(pointArret);
        }
        trajet.setPointsArret(pointsArretEntites);

        Trajet sauvegarde = trajetRepository.save(trajet);

        log.info("Trajet publié avec l'id {} par le conducteur {}", sauvegarde.getId(), conducteur.getId());

        return construireReponseAvecPlacesRestantes(sauvegarde);
    }

    @Override
    public TrajetResponseDTO consulterDetail(Long id) {
        Trajet trajet = trajetRepository.findDetailById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Trajet introuvable avec l'id : " + id));
        return construireReponseAvecPlacesRestantes(trajet);
    }

    @Override
    public Page<TrajetRechercheResponseDTO> rechercher(
            String villeDepart,
            String villeArrivee,
            LocalDate date,
            int placesDemandees,
            BigDecimal prixMax,
            int page,
            int taille) {

        Pageable pageable = PageRequest.of(page, taille);

        return trajetRepository.rechercher(
                villeDepart, villeArrivee, date, placesDemandees, prixMax, pageable);
    }

    // construit le DTO et  ajoute les places restantes calculees
    private TrajetResponseDTO construireReponseAvecPlacesRestantes(Trajet trajet) {
        TrajetResponseDTO dtoSansPlaces = trajetMapper.toResponseDTO(trajet);

        int placesReservees = trajetRepository.compterPlacesReservees(trajet.getId());
        int placesRestantes = trajet.getPlacesProposees() - placesReservees;

        return new TrajetResponseDTO(
                dtoSansPlaces.id(),
                dtoSansPlaces.villeDepart(),
                dtoSansPlaces.villeArrivee(),
                dtoSansPlaces.dateDepart(),
                dtoSansPlaces.placesProposees(),
                placesRestantes,
                dtoSansPlaces.statut(),
                dtoSansPlaces.conducteurId(),
                dtoSansPlaces.nomConducteur(),
                dtoSansPlaces.vehiculeId(),
                dtoSansPlaces.pointsArret()
        );
    }
}