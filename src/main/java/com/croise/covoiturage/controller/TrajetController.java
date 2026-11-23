package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.service.TrajetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Trajets",
        description = "Publication, recherche et cycle de vie des trajets. Codes HTTP utilisés : 200 (succès), 201 (créé), 204 (terminé/annulé), 400 (parcours invalide), 403 (véhicule/trajet n'appartenant pas au conducteur), 404 (introuvable), 409 (règle métier violée : capacité, statut final, délai).")
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    @Operation(summary = "Publier un trajet", description = "Au moins 2 points d'arrêt, heures et prix strictement croissants, prix du premier point = 0, départ dans le futur, places ≤ capacité du véhicule, véhicule appartenant au conducteur.")
    public ResponseEntity<TrajetResponseDTO> publier(@Valid @RequestBody TrajetRequestDTO dto) {
        TrajetResponseDTO reponse = trajetService.publier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'un trajet", description = "Parcours complet, places restantes calculées, conducteur et véhicule.")
    public ResponseEntity<TrajetResponseDTO> consulterDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.consulterDetail(id));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des trajets", description = "Par ville de départ/arrivée (n'importe quels points du parcours, dans le bon sens), date optionnelle, places demandées, prix max sur le segment. Résultats paginés, triés par heure de départ.")
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
    @Operation(summary = "Terminer un trajet", description = "Réservé au conducteur propriétaire, uniquement après l'heure de départ.")
    public ResponseEntity<Void> terminer(@PathVariable Long id, @RequestParam Long conducteurId) {
        trajetService.terminer(id, conducteurId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/annuler")
    @Operation(summary = "Annuler un trajet", description = "Réservé au conducteur propriétaire. Annule en cascade toutes les réservations CONFIRMEE du trajet.")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestParam Long conducteurId) {
        trajetService.annulerTrajet(id, conducteurId);
        return ResponseEntity.noContent().build();
    }
}