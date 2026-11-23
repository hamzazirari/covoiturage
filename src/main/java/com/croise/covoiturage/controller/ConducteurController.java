package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.ConducteurRequestDTO;
import com.croise.covoiturage.dto.ConducteurResponseDTO;
import com.croise.covoiturage.service.ConducteurService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;



import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
@Tag(
        name = "Conducteurs",
        description = "Gestion des profils conducteurs. Codes HTTP utilisés : 200 (succès), 201 (créé), 400 (validation), 404 (introuvable), 409 (email/permis déjà utilisé).")
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    @Operation(summary = "Créer un conducteur", description = "Email et numéro de permis doivent être uniques.")
    public ResponseEntity<ConducteurResponseDTO> creer(@Valid @RequestBody ConducteurRequestDTO dto) {
        ConducteurResponseDTO reponse = conducteurService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un conducteur par son id")
    public ResponseEntity<ConducteurResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.consulter(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les conducteurs")
    public ResponseEntity<List<ConducteurResponseDTO>> listerTous() {
        return ResponseEntity.ok(conducteurService.listerTous());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier le profil d'un conducteur")
    public ResponseEntity<ConducteurResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequestDTO dto) {
        return ResponseEntity.ok(conducteurService.modifier(id, dto));
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher un conducteur par nom ou email")
    public ResponseEntity<List<ConducteurResponseDTO>> rechercher(@RequestParam String motCle) {
        return ResponseEntity.ok(conducteurService.rechercher(motCle));
    }
}