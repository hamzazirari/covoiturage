package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.PointArretRequestDTO;
import com.croise.covoiturage.dto.PointArretResponseDTO;
import com.croise.covoiturage.entity.PointArret;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    PointArretResponseDTO toResponseDTO(PointArret entity);

    // On ignore "trajet" et "ordre" : ils seront fixés manuellement dans le service
    @org.mapstruct.Mapping(target = "trajet", ignore = true)
    @org.mapstruct.Mapping(target = "ordre", ignore = true)
    @org.mapstruct.Mapping(target = "id", ignore = true)
    PointArret toEntity(PointArretRequestDTO dto);
}