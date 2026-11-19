package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    List<Vehicule> findByConducteurId(Long conducteurId);

    // Vérifie si ce véhicule est utilisé par au moins un trajet (peu importe son statut)
    @Query("SELECT COUNT(t) > 0 FROM Trajet t WHERE t.vehicule.id = :vehiculeId")
    boolean estAssocieAUnTrajet(@Param("vehiculeId") Long vehiculeId);
}