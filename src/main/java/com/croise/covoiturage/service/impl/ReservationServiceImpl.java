package com.croise.covoiturage.service.impl;

import com.croise.covoiturage.dto.ReservationRequestDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.entity.*;
import com.croise.covoiturage.enums.StatutReservation;
import com.croise.covoiturage.enums.StatutTrajet;
import com.croise.covoiturage.exception.*;
import com.croise.covoiturage.mapper.ReservationMapper;
import com.croise.covoiturage.repository.*;
import com.croise.covoiturage.service.ReservationService;
import com.sun.java.accessibility.util.Translator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResponseDTO reserver(ReservationRequestDTO dto) {

        // --- 1. Verifier que le passager existe ---
        Passager passager = passagerRepository.findById(dto.passagerId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Passager introuvable avec l'id : " + dto.passagerId()));

        // --- 2. Verifier que le trajet existe ---
        Trajet trajet = trajetRepository.findById(dto.trajetId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Trajet introuvable avec l'id : " + dto.trajetId()));

        // --- 3 Regle : reserve que sur un trajet PUBLIE
        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn("Tentative de réservation sur un trajet non PUBLIE (statut actuel : {}), trajet {}",
                    trajet.getStatut(), trajet.getId());
            throw new RegleMetierException("Ce trajet n'est pas ouvert à la réservation (statut : " + trajet.getStatut() + ")");
        }

        // --- 4 Verifier que les point existe
        PointArret pointMontee = pointArretRepository.findById(dto.pointMonteeId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Point de montée introuvable avec l'id : " + dto.pointMonteeId()));

        PointArret pointDescente = pointArretRepository.findById(dto.pointDescenteId())
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Point de descente introuvable avec l'id : " + dto.pointDescenteId()));

        // --- 5 les deux points appartiennent au trajet
        if (!pointMontee.getTrajet().getId().equals(trajet.getId())
                || !pointDescente.getTrajet().getId().equals(trajet.getId())) {
            throw new ValidationMetierException("Les points de montée et de descente doivent appartenir au trajet demandé");
        }

        //  6 la montee est avant la descente
        if (pointMontee.getOrdre() >= pointDescente.getOrdre()) {
            throw new ValidationMetierException("Le point de montée doit être avant le point de descente");
        }

        // --- 7 un passager a que une seule reservation CONFIRMEE f trajet
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(
                trajet.getId(), passager.getId(), StatutReservation.CONFIRMEE)) {
            log.warn("Le passager {} a déjà une réservation confirmée sur le trajet {}", passager.getId(), trajet.getId());
            throw new RegleMetierException("Vous avez déjà une réservation confirmée sur ce trajet");
        }

        // --- 8 le nombre de places reserve ne depasse pas les places proposees (409)
        int placesReservees = calculerPlacesReservees(trajet.getId());
        int placesRestantes = trajet.getPlacesProposees() - placesReservees;

        if (dto.nombrePlaces() > placesRestantes) {
            log.warn("Places demandées ({}) supérieures aux places restantes ({}) sur le trajet {}",
                    dto.nombrePlaces(), placesRestantes, trajet.getId());
            throw new RegleMetierException("Il ne reste que " + placesRestantes + " place(s) sur ce trajet");
        }

        // --- 9 (prix descente - prix montée) x nombre de places
        BigDecimal montant = pointDescente.getPrixDepuisDepart()
                .subtract(pointMontee.getPrixDepuisDepart())
                .multiply(BigDecimal.valueOf(dto.nombrePlaces()));

        // --- 10 Construction sauvegarde dial reservation
        Reservation reservation = new Reservation();
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(pointMontee);
        reservation.setPointDescente(pointDescente);
        reservation.setNombrePlaces(dto.nombrePlaces());
        reservation.setMontantTotal(montant);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation sauvegarde = reservationRepository.save(reservation);

        // --- 11 mise ajour le statut du trajet si complet
        int nouvellesPlacesRestantes = placesRestantes - dto.nombrePlaces();
        if (nouvellesPlacesRestantes == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("Le trajet {} est maintenant COMPLET", trajet.getId());
        }

        log.info("Réservation créée avec l'id {} pour le passager {} sur le trajet {}, montant {} DH",
                sauvegarde.getId(), passager.getId(), trajet.getId(), montant);

        return reservationMapper.toResponseDTO(
                reservationRepository.findDetailById(sauvegarde.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public void annuler(Long reservationId, Long passagerId) {

        Reservation reservation = reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Réservation introuvable avec l'id : " + reservationId));

        // -- seul le passager le main peut annuler sa reservation
        if (!reservation.getPassager().getId().equals(passagerId)) {
            log.warn("Le passager {} a tenté d'annuler la réservation {} qui ne lui appartient pas",
                    passagerId, reservationId);
            throw new AccesInterditException("Cette réservation ne vous appartient pas");
        }

        // ---  impossible de annuler une reservation deja kant annule
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RegleMetierException("Cette réservation est déjà annulée");
        }

        // ---  annulation avant  24h avant le depart seulement
        LocalDateTime dateLimite = reservation.getTrajet().getDateDepart().minusHours(24);
        if (LocalDateTime.now().isAfter(dateLimite)) {
            log.warn("Tentative d'annulation hors délai pour la réservation {}", reservationId);
            throw new RegleMetierException("Le délai d'annulation de 24 heures avant le départ est dépassé");
        }

        Trajet trajet = reservation.getTrajet();
        StatutTrajet ancienStatutTrajet = trajet.getStatut();

        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);

        // --- Libérer une place : si le trajet était COMPLET, il redevient PUBLIE ---
        if (ancienStatutTrajet == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet);
            log.info("Le trajet {} redevient PUBLIE après une annulation", trajet.getId());
        }

        log.info("Réservation {} annulée par le passager {}", reservationId, passagerId);
    }

    @Override
    public List<ReservationResponseDTO> consulterParPassager(Long passagerId) {

        if (!passagerRepository.existsById(passagerId)) {
            throw new RessourceNotFoundException("Passager introuvable avec l'id : " + passagerId);
        }

        return reservationRepository.findByPassagerId(passagerId)
                .stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<ReservationResponseDTO> consulterParTrajet(Long trajetId, Long conducteurId) {

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RessourceNotFoundException("Trajet introuvable avec l'id : " + trajetId));

        // --- Règle : seul le conducteur propriétaire du trajet voit ses réservations (403) ---
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            throw new AccesInterditException("Ce trajet ne vous appartient pas");
        }

        return reservationRepository.findByTrajetId(trajetId)
                .stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    // Méthode utilitaire : calcule les places déjà réservées (statut CONFIRMEE) sur un trajet
    private int calculerPlacesReservees(Long trajetId) {
        return reservationRepository.findByTrajetId(trajetId)
                .stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
    }
}