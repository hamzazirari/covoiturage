package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.VehiculeRequestDTO;
import com.croise.covoiturage.dto.VehiculeResponseDTO;
import com.croise.covoiturage.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
@Tag(
        name = "Véhicules",
        description = "Gestion des véhicules des conducteurs. Codes HTTP utilisés : 200 (succès), 201 (créé), 204 (supprimé), 400 (validation, ex: capacité hors limites), 404 (introuvable), 409 (immatriculation déjà utilisée ou véhicule associé à un trajet).")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    @Operation(summary = "Ajouter un véhicule", description = "L'immatriculation doit être unique. Capacité entre 1 et 8 places.")

    public ResponseEntity<VehiculeResponseDTO> ajouter(@Valid @RequestBody VehiculeRequestDTO dto) {
        VehiculeResponseDTO reponse = vehiculeService.ajouter(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un véhicule par son id")
    public ResponseEntity<VehiculeResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.consulter(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un véhicule")
    public ResponseEntity<VehiculeResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequestDTO dto) {
        return ResponseEntity.ok(vehiculeService.modifier(id, dto));
    }

    @GetMapping("/conducteur/{conducteurId}")
    @Operation(summary = "Lister les véhicules d'un conducteur")
    public ResponseEntity<List<VehiculeResponseDTO>> listerParConducteur(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(vehiculeService.listerParConducteur(conducteurId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule", description = "Possible uniquement si le véhicule n'est associé à aucun trajet.")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        vehiculeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}