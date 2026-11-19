package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.VehiculeRequestDTO;
import com.croise.covoiturage.dto.VehiculeResponseDTO;
import com.croise.covoiturage.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "conducteur", ignore = true)
    Vehicule toEntity(VehiculeRequestDTO dto);

    @Mapping(source = "conducteur.id", target = "conducteurId")
    VehiculeResponseDTO toResponseDTO(Vehicule entity);
}