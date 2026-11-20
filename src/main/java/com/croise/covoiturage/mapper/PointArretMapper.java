package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.PointArretRequestDTO;
import com.croise.covoiturage.dto.PointArretResponseDTO;
import com.croise.covoiturage.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    PointArretResponseDTO toResponseDTO(PointArret entity);

    // ignore "trajet" et "ordre", ils seront fixes manuellement dans le service

    @Mapping(target = "trajet", ignore = true)
    @Mapping(target = "ordre", ignore = true)
    @Mapping(target = "id", ignore = true)
    PointArret toEntity(PointArretRequestDTO dto);
}