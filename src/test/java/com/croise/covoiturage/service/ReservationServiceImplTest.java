package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.ReservationRequestDTO;
import com.croise.covoiturage.dto.ReservationResponseDTO;
import com.croise.covoiturage.entity.*;
import com.croise.covoiturage.enums.StatutReservation;
import com.croise.covoiturage.enums.StatutTrajet;
import com.croise.covoiturage.exception.AccesInterditException;
import com.croise.covoiturage.exception.RegleMetierException;
import com.croise.covoiturage.exception.ValidationMetierException;
import com.croise.covoiturage.mapper.ReservationMapper;
import com.croise.covoiturage.repository.*;
import com.croise.covoiturage.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Passager passager;
    private Trajet trajet;
    private PointArret pointMontee;
    private PointArret pointDescente;

    @BeforeEach
    void setUp() {
        passager = new Passager();
        passager.setId(1L);
        passager.setNom("Salma");

        trajet = new Trajet();
        trajet.setId(1L);
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setPlacesProposees(3);
        trajet.setDateDepart(LocalDateTime.now().plusDays(2));

        pointMontee = new PointArret();
        pointMontee.setId(3L);
        pointMontee.setTrajet(trajet);
        pointMontee.setOrdre(2);
        pointMontee.setPrixDepuisDepart(BigDecimal.valueOf(70));

        pointDescente = new PointArret();
        pointDescente.setId(4L);
        pointDescente.setTrajet(trajet);
        pointDescente.setOrdre(3);
        pointDescente.setPrixDepuisDepart(BigDecimal.valueOf(120));
    }

    @Test
    void reserver_casNominal_doitReussirEtCalculerLeBonMontant() {
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, 1L, 3L, 4L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(3L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(4L)).thenReturn(Optional.of(pointDescente));
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(1L, 1L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        when(reservationRepository.findByTrajetId(1L)).thenReturn(List.of());

        Reservation reservationSauvegardee = new Reservation();
        reservationSauvegardee.setId(100L);
        reservationSauvegardee.setMontantTotal(BigDecimal.valueOf(50));
        when(reservationRepository.save(any())).thenReturn(reservationSauvegardee);
        when(reservationRepository.findDetailById(100L)).thenReturn(Optional.of(reservationSauvegardee));

        ReservationResponseDTO reponseAttendue = new ReservationResponseDTO(
                100L, 1L, "Salma", 1L, "Settat", "Marrakech", 1,
                BigDecimal.valueOf(50), LocalDateTime.now(), StatutReservation.CONFIRMEE);
        when(reservationMapper.toResponseDTO(reservationSauvegardee)).thenReturn(reponseAttendue);

        ReservationResponseDTO resultat = reservationService.reserver(dto);

        assertThat(resultat).isNotNull();
        assertThat(resultat.montantTotal()).isEqualByComparingTo(BigDecimal.valueOf(50));

        // Vérifie que le trajet n'a pas été mis en COMPLET (il reste 2 places sur 3)
        verify(trajetRepository, never()).save(argThat(t -> t.getStatut() == StatutTrajet.COMPLET));
    }

    @Test
    void reserver_trajetNonPublie_doitLeverRegleMetier() {
        trajet.setStatut(StatutTrajet.COMPLET);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, 1L, 3L, 4L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("pas ouvert à la réservation");
    }

    @Test
    void reserver_monteeApresDescente_doitLeverValidationMetier() {
        // on inverse : montée = pointDescente (ordre 3), descente = pointMontee (ordre 2)
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, 1L, 4L, 3L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(4L)).thenReturn(Optional.of(pointDescente)); // devient "montée" dans la requête
        when(pointArretRepository.findById(3L)).thenReturn(Optional.of(pointMontee));   // devient "descente" dans la requête

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(ValidationMetierException.class)
                .hasMessageContaining("avant le point de descente");
    }

    @Test
    void reserver_dejaUneReservationConfirmee_doitLeverRegleMetier() {
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, 1L, 3L, 4L, 1);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(3L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(4L)).thenReturn(Optional.of(pointDescente));
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(1L, 1L, StatutReservation.CONFIRMEE))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("déjà une réservation confirmée");
    }

    @Test
    void reserver_placesInsuffisantes_doitLeverRegleMetier() {
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, 1L, 3L, 4L, 5); // 5 places demandées, 3 proposées

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(3L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(4L)).thenReturn(Optional.of(pointDescente));
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(1L, 1L, StatutReservation.CONFIRMEE))
                .thenReturn(false);
        when(reservationRepository.findByTrajetId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.reserver(dto))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("Il ne reste que");
    }

    @Test
    void annuler_delaiDepasse_doitLeverRegleMetier() {
        trajet.setDateDepart(LocalDateTime.now().plusHours(2)); // départ dans 2h seulement

        Reservation reservation = new Reservation();
        reservation.setId(50L);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annuler(50L, 1L))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("délai d'annulation");
    }

    @Test
    void annuler_passagerNonProprietaire_doitLeverAccesInterdit() {
        Reservation reservation = new Reservation();
        reservation.setId(50L);
        reservation.setPassager(passager); // appartient au passager 1L
        reservation.setTrajet(trajet);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.annuler(50L, 99L)) // un autre passager
                .isInstanceOf(AccesInterditException.class);
    }

    @Test
    void annuler_casNominal_doitChangerLeStatutDuTrajetSiComplet() {
        trajet.setStatut(StatutTrajet.COMPLET); // trajet complet avant l'annulation
        trajet.setDateDepart(LocalDateTime.now().plusDays(3)); // bien dans le délai

        Reservation reservation = new Reservation();
        reservation.setId(50L);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        when(reservationRepository.findDetailById(50L)).thenReturn(Optional.of(reservation));

        reservationService.annuler(50L, 1L);

        // Vérifie que le trajet est repassé en PUBLIE
        verify(trajetRepository).save(argThat(t -> t.getStatut() == StatutTrajet.PUBLIE));
        // Vérifie que la réservation a été marquée ANNULEE
        verify(reservationRepository).save(argThat(r -> r.getStatut() == StatutReservation.ANNULEE));
    }
}