package com.croise.covoiturage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequestDTO(

        @NotNull(message = "L'id du passager est obligatoire")
        Long passagerId,

        @NotNull(message = "L'id du trajet est obligatoire")
        Long trajetId,

        @NotNull(message = "Le point de montée est obligatoire")
        Long pointMonteeId,

        @NotNull(message = "Le point de descente est obligatoire")
        Long pointDescenteId,

        @Min(value = 1, message = "Il faut réserver au moins 1 place")
        int nombrePlaces
) {}