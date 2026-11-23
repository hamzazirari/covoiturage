package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.PassagerRequestDTO;
import com.croise.covoiturage.dto.PassagerResponseDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.service.PassagerService;
import com.croise.covoiturage.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    public ResponseEntity<PassagerResponseDTO> creer(@Valid @RequestBody PassagerRequestDTO dto) {
        PassagerResponseDTO reponse = passagerService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassagerResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.consulter(id));
    }

    @GetMapping
    public ResponseEntity<List<PassagerResponseDTO>> listerTous() {
        return ResponseEntity.ok(passagerService.listerTous());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassagerResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequestDTO dto) {
        return ResponseEntity.ok(passagerService.modifier(id, dto));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<PassagerResponseDTO>> rechercherParEmail(@RequestParam String email) {
        return ResponseEntity.ok(passagerService.rechercherParEmail(email));
    }

    private final ReservationService reservationService;

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponseDTO>> consulterHistoriqueReservations(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.consulterParPassager(id));
    }
}