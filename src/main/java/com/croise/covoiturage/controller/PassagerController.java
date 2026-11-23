package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.PassagerRequestDTO;
import com.croise.covoiturage.dto.PassagerResponseDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.service.PassagerService;
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
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
@Tag(
        name = "Passagers",
        description = "Gestion des profils passagers. Codes HTTP utilisés : 200 (succès), 201 (créé), 400 (validation), 404 (introuvable), 409 (email déjà utilisé).")
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    @Operation(summary = "Créer un passager", description = "L'email doit être unique.")
    public ResponseEntity<PassagerResponseDTO> creer(@Valid @RequestBody PassagerRequestDTO dto) {
        PassagerResponseDTO reponse = passagerService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un passager par son id")
    public ResponseEntity<PassagerResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(passagerService.consulter(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les passagers")
    public ResponseEntity<List<PassagerResponseDTO>> listerTous() {
        return ResponseEntity.ok(passagerService.listerTous());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un passager")

    public ResponseEntity<PassagerResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PassagerRequestDTO dto) {
        return ResponseEntity.ok(passagerService.modifier(id, dto));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un passager par email")

    public ResponseEntity<List<PassagerResponseDTO>> rechercherParEmail(@RequestParam String email) {
        return ResponseEntity.ok(passagerService.rechercherParEmail(email));
    }

    private final ReservationService reservationService;

    @GetMapping("/{id}/reservations")
    @Operation(summary = "Consulter l'historique des réservations d'un passager")

    public ResponseEntity<List<ReservationResponseDTO>> consulterHistoriqueReservations(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.consulterParPassager(id));
    }
}