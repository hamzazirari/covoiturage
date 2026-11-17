package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.ConducteurRequestDTO;
import com.croise.covoiturage.dto.ConducteurResponseDTO;
import com.croise.covoiturage.entity.Conducteur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {

    Conducteur toEntity(ConducteurRequestDTO dto);

    ConducteurResponseDTO toResponseDTO(Conducteur entity);
}