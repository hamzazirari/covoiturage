package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    // Charge le trajet + conducteur + véhicule + points d'arrêt en UNE SEULE requête SQL (pas de N+1)
    @Query("""
            SELECT DISTINCT t FROM Trajet t
            JOIN FETCH t.conducteur
            JOIN FETCH t.vehicule
            JOIN FETCH t.pointsArret
            WHERE t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);


}