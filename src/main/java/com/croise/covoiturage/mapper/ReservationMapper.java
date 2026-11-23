package com.croise.covoiturage.mapper;

import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "passager.id", target = "passagerId")
    @Mapping(target = "nomPassager", expression = "java(entity.getPassager().getNom() + \" \" + entity.getPassager().getPrenom())")
    @Mapping(source = "trajet.id", target = "trajetId")
    @Mapping(source = "pointMontee.ville", target = "villeMontee")
    @Mapping(source = "pointDescente.ville", target = "villeDescente")
    ReservationResponseDTO toResponseDTO(Reservation entity);
}