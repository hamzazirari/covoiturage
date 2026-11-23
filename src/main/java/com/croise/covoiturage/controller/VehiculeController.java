package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.VehiculeRequestDTO;
import com.croise.covoiturage.dto.VehiculeResponseDTO;
import com.croise.covoiturage.service.VehiculeService;
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
@Tag(name = "Véhicules", description = "Gestion des véhicules des conducteurs")

public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponseDTO> ajouter(@Valid @RequestBody VehiculeRequestDTO dto) {
        VehiculeResponseDTO reponse = vehiculeService.ajouter(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.consulter(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeRequestDTO dto) {
        return ResponseEntity.ok(vehiculeService.modifier(id, dto));
    }

    @GetMapping("/conducteur/{conducteurId}")
    public ResponseEntity<List<VehiculeResponseDTO>> listerParConducteur(@PathVariable Long conducteurId) {
        return ResponseEntity.ok(vehiculeService.listerParConducteur(conducteurId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        vehiculeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}