package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.croise.covoiturage.dto.TrajetRechercheResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    public ResponseEntity<TrajetResponseDTO> publier(@Valid @RequestBody TrajetRequestDTO dto) {
        TrajetResponseDTO reponse = trajetService.publier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> consulterDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.consulterDetail(id));
    }

    @GetMapping("/recherche")
    public ResponseEntity<Page<TrajetRechercheResponseDTO>> rechercher(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int placesDemandees,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {

        Page<TrajetRechercheResponseDTO> resultats = trajetService.rechercher(
                villeDepart, villeArrivee, date, placesDemandees, prixMax, page, taille);

        return ResponseEntity.ok(resultats);
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<Void> terminer(@PathVariable Long id, @RequestParam Long conducteurId) {
        trajetService.terminer(id, conducteurId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestParam Long conducteurId) {
        trajetService.annulerTrajet(id, conducteurId);
        return ResponseEntity.noContent().build();
    }
}