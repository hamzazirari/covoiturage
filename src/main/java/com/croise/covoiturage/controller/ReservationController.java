package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.ReservationRequestDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(
        name = "Réservations",
        description = "Réservation et annulation de places sur un trajet. Codes HTTP utilisés : 200 (succès), 201 (créé), 204 (annulé), 400 (montée après descente, points invalides), 403 (réservation/trajet n'appartenant pas à l'appelant), 404 (introuvable), 409 (trajet non PUBLIE, déjà réservé, places insuffisantes, délai de 24h dépassé).")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Réserver des places sur un trajet", description = "Trajet doit être PUBLIE, montée avant descente, 1 seule réservation CONFIRMEE par passager et par trajet, places disponibles suffisantes. Montant = (prix descente - prix montée) × nombre de places.")
    public ResponseEntity<ReservationResponseDTO> reserver(@Valid @RequestBody ReservationRequestDTO dto) {
        ReservationResponseDTO reponse = reservationService.reserver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une réservation", description = "Possible jusqu'à 24h avant le départ. Libère une place et repasse le trajet en PUBLIE si besoin.")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        reservationService.annuler(id, passagerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/passager/{passagerId}")
    @Operation(summary = "Consulter les réservations d'un passager")
    public ResponseEntity<List<ReservationResponseDTO>> consulterParPassager(@PathVariable Long passagerId) {
        return ResponseEntity.ok(reservationService.consulterParPassager(passagerId));
    }

    @GetMapping("/trajet/{trajetId}")
    @Operation(summary = "Consulter les réservations d'un trajet", description = "Réservé au conducteur propriétaire du trajet.")
    public ResponseEntity<List<ReservationResponseDTO>> consulterParTrajet(
            @PathVariable Long trajetId,
            @RequestParam Long conducteurId) {
        return ResponseEntity.ok(reservationService.consulterParTrajet(trajetId, conducteurId));
    }
}