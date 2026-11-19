package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.PassagerRequestDTO;
import com.croise.covoiturage.dto.PassagerResponseDTO;
import com.croise.covoiturage.entity.Passager;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    Passager toEntity(PassagerRequestDTO dto);

    PassagerResponseDTO toResponseDTO(Passager entity);
}