package com.croise.covoiturage.controller;

import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}