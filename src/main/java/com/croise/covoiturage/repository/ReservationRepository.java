package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Reservation;
import com.croise.covoiturage.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTrajetIdAndPassagerIdAndStatut(
            Long trajetId, Long passagerId, StatutReservation statut);

    // Charge la réservation + passager + trajet + points en une seule requête (pas de N+1)
    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.passager
            JOIN FETCH r.trajet
            JOIN FETCH r.pointMontee
            JOIN FETCH r.pointDescente
            WHERE r.id = :id
            """)
    Optional<Reservation> findDetailById(@Param("id") Long id);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.trajet
            JOIN FETCH r.pointMontee
            JOIN FETCH r.pointDescente
            WHERE r.passager.id = :passagerId
            ORDER BY r.dateReservation DESC
            """)
    List<Reservation> findByPassagerId(@Param("passagerId") Long passagerId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.passager
            JOIN FETCH r.pointMontee
            JOIN FETCH r.pointDescente
            WHERE r.trajet.id = :trajetId
            ORDER BY r.dateReservation DESC
            """)
    List<Reservation> findByTrajetId(@Param("trajetId") Long trajetId);
}