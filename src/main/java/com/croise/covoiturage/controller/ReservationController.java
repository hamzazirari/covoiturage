package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.ReservationRequestDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> reserver(@Valid @RequestBody ReservationRequestDTO dto) {
        ReservationResponseDTO reponse = reservationService.reserver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        reservationService.annuler(id, passagerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/passager/{passagerId}")
    public ResponseEntity<List<ReservationResponseDTO>> consulterParPassager(@PathVariable Long passagerId) {
        return ResponseEntity.ok(reservationService.consulterParPassager(passagerId));
    }

    @GetMapping("/trajet/{trajetId}")
    public ResponseEntity<List<ReservationResponseDTO>> consulterParTrajet(
            @PathVariable Long trajetId,
            @RequestParam Long conducteurId) {
        return ResponseEntity.ok(reservationService.consulterParTrajet(trajetId, conducteurId));
    }
}