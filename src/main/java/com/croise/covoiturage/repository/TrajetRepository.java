package com.croise.covoiturage.repository;

import com.croise.covoiturage.dto.TrajetRechercheResponseDTO;
import com.croise.covoiturage.entity.Trajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    @Query("""
            SELECT DISTINCT t FROM Trajet t
            JOIN FETCH t.conducteur
            JOIN FETCH t.vehicule
            JOIN FETCH t.pointsArret
            WHERE t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);

    @Query("""
            SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r
            WHERE r.trajet.id = :trajetId
            AND r.statut = com.croise.covoiturage.enums.StatutReservation.CONFIRMEE
            """)
    int compterPlacesReservees(@Param("trajetId") Long trajetId);

    @Query("""
        SELECT new com.croise.covoiturage.dto.TrajetRechercheResponseDTO(
            t.id,
            pDepart.ville,
            pArrivee.ville,
            t.dateDepart,
            (pArrivee.prixDepuisDepart - pDepart.prixDepuisDepart),
            (t.placesProposees - COALESCE(
                (SELECT SUM(r.nombrePlaces) FROM Reservation r
                 WHERE r.trajet = t
                 AND r.statut = com.croise.covoiturage.enums.StatutReservation.CONFIRMEE), 0)),
            t.statut
        )
        FROM Trajet t
        JOIN t.pointsArret pDepart
        JOIN t.pointsArret pArrivee
        WHERE LOWER(pDepart.ville) = LOWER(:villeDepart)
        AND LOWER(pArrivee.ville) = LOWER(:villeArrivee)
        AND pDepart.ordre < pArrivee.ordre
        AND t.statut = com.croise.covoiturage.enums.StatutTrajet.PUBLIE
        AND FUNCTION('DATE', t.dateDepart) = COALESCE(:date, FUNCTION('DATE', t.dateDepart))
        AND (t.placesProposees - COALESCE(
                (SELECT SUM(r.nombrePlaces) FROM Reservation r
                 WHERE r.trajet = t
                 AND r.statut = com.croise.covoiturage.enums.StatutReservation.CONFIRMEE), 0)
            ) >= :placesDemandees
        AND (pArrivee.prixDepuisDepart - pDepart.prixDepuisDepart) <= COALESCE(:prixMax, pArrivee.prixDepuisDepart - pDepart.prixDepuisDepart)
        ORDER BY t.dateDepart ASC
        """)
    Page<TrajetRechercheResponseDTO> rechercher(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("date") LocalDate date,
            @Param("placesDemandees") int placesDemandees,
            @Param("prixMax") BigDecimal prixMax,
            Pageable pageable
    );
}