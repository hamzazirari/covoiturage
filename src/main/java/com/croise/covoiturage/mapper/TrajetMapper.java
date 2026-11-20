package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PointArretMapper.class)
public interface TrajetMapper {

    @Mapping(source = "conducteur.id", target = "conducteurId")
    @Mapping(target = "nomConducteur", expression = "java(entity.getConducteur().getNom() + \" \" + entity.getConducteur().getPrenom())")
    @Mapping(source = "vehicule.id", target = "vehiculeId")
    @Mapping(target = "placesRestantes", ignore = true) // calculé à part dans le service
    TrajetResponseDTO toResponseDTO(Trajet entity);
}