package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.ConducteurRequestDTO;
import com.croise.covoiturage.dto.ConducteurResponseDTO;
import com.croise.covoiturage.service.ConducteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    public ResponseEntity<ConducteurResponseDTO> creer(@Valid @RequestBody ConducteurRequestDTO dto) {
        ConducteurResponseDTO reponse = conducteurService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponseDTO> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(conducteurService.consulter(id));
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponseDTO>> listerTous() {
        return ResponseEntity.ok(conducteurService.listerTous());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConducteurResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ConducteurRequestDTO dto) {
        return ResponseEntity.ok(conducteurService.modifier(id, dto));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ConducteurResponseDTO>> rechercher(@RequestParam String motCle) {
        return ResponseEntity.ok(conducteurService.rechercher(motCle));
    }
}