package com.croise.covoiturage.service;

import com.croise.covoiturage.dto.PointArretRequestDTO;
import com.croise.covoiturage.dto.PointArretResponseDTO;
import com.croise.covoiturage.dto.TrajetRequestDTO;
import com.croise.covoiturage.dto.TrajetResponseDTO;
import com.croise.covoiturage.entity.Conducteur;
import com.croise.covoiturage.entity.PointArret;
import com.croise.covoiturage.entity.Trajet;
import com.croise.covoiturage.entity.Vehicule;
import com.croise.covoiturage.enums.StatutTrajet;
import com.croise.covoiturage.exception.AccesInterditException;
import com.croise.covoiturage.exception.RessourceNotFoundException;
import com.croise.covoiturage.exception.ValidationMetierException;
import com.croise.covoiturage.mapper.PointArretMapper;
import com.croise.covoiturage.mapper.TrajetMapper;
import com.croise.covoiturage.repository.ConducteurRepository;
import com.croise.covoiturage.repository.ReservationRepository;
import com.croise.covoiturage.repository.TrajetRepository;
import com.croise.covoiturage.repository.VehiculeRepository;
import com.croise.covoiturage.service.impl.TrajetServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private TrajetMapper trajetMapper;

    @Mock
    private PointArretMapper pointArretMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    private Conducteur conducteur;
    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        conducteur = new Conducteur();
        conducteur.setId(1L);
        conducteur.setNom("Hamid");

        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setCapacite(4);
        vehicule.setConducteur(conducteur);
    }

    // Construit une liste de points valides : Rabat(0) -> Casablanca(40) -> Settat(70) -> Marrakech(120)
    private List<PointArretRequestDTO> pointsValides() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        return List.of(
                new PointArretRequestDTO("Rabat", base, BigDecimal.ZERO),
                new PointArretRequestDTO("Casablanca", base.plusHours(1), BigDecimal.valueOf(40)),
                new PointArretRequestDTO("Settat", base.plusHours(2), BigDecimal.valueOf(70)),
                new PointArretRequestDTO("Marrakech", base.plusHours(4), BigDecimal.valueOf(120))
        );
    }

    @Test
    void publier_casNominal_doitReussir() {
        // ARRANGE : préparer les données et le comportement des mocks
        TrajetRequestDTO dto = new TrajetRequestDTO(1L, 1L, 3, pointsValides());

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(pointArretMapper.toEntity(any())).thenReturn(new PointArret());

        Trajet trajetSauvegarde = new Trajet();
        trajetSauvegarde.setId(10L);
        trajetSauvegarde.setPlacesProposees(3);
        when(trajetRepository.save(any())).thenReturn(trajetSauvegarde);

        when(trajetRepository.compterPlacesReservees(10L)).thenReturn(0);

        TrajetResponseDTO reponseAttendue = new TrajetResponseDTO(
                10L, "Rabat", "Marrakech", LocalDateTime.now(), 3, 0,
                StatutTrajet.PUBLIE, 1L, "Hamid", 1L, List.of());
        when(trajetMapper.toResponseDTO(trajetSauvegarde)).thenReturn(reponseAttendue);

        // ACT : appeler la méthode testée
        TrajetResponseDTO resultat = trajetService.publier(dto);

        // ASSERT : vérifier le résultat
        assertThat(resultat).isNotNull();
        assertThat(resultat.villeDepart()).isEqualTo("Rabat");
        assertThat(resultat.villeArrivee()).isEqualTo("Marrakech");
        assertThat(resultat.placesRestantes()).isEqualTo(3);
    }

    @Test
    void publier_vehiculeAppartientAUnAutreConducteur_doitLeverAccesInterdit() {
        Conducteur autreConducteur = new Conducteur();
        autreConducteur.setId(2L);

        TrajetRequestDTO dto = new TrajetRequestDTO(2L, 1L, 3, pointsValides());

        when(conducteurRepository.findById(2L)).thenReturn(Optional.of(autreConducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule)); // vehicule appartient au conducteur 1L

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(AccesInterditException.class)
                .hasMessageContaining("n'appartient pas");
    }

    @Test
    void publier_capaciteDepassee_doitLeverValidationMetier() {
        TrajetRequestDTO dto = new TrajetRequestDTO(1L, 1L, 5, pointsValides()); // 5 places, capacité 4

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(ValidationMetierException.class)
                .hasMessageContaining("capacité");
    }

    @Test
    void publier_moinsDeDeuxPoints_doitLeverValidationMetier() {
        List<PointArretRequestDTO> unSeulPoint = List.of(
                new PointArretRequestDTO("Rabat", LocalDateTime.now().plusDays(1), BigDecimal.ZERO)
        );
        TrajetRequestDTO dto = new TrajetRequestDTO(1L, 1L, 2, unSeulPoint);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(ValidationMetierException.class)
                .hasMessageContaining("au moins 2 points");
    }

    @Test
    void publier_departDansLePasse_doitLeverValidationMetier() {
        LocalDateTime passe = LocalDateTime.now().minusDays(1);
        List<PointArretRequestDTO> points = List.of(
                new PointArretRequestDTO("Rabat", passe, BigDecimal.ZERO),
                new PointArretRequestDTO("Marrakech", passe.plusHours(3), BigDecimal.valueOf(100))
        );
        TrajetRequestDTO dto = new TrajetRequestDTO(1L, 1L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(ValidationMetierException.class)
                .hasMessageContaining("futur");
    }

    @Test
    void publier_villeEnDouble_doitLeverValidationMetier() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        List<PointArretRequestDTO> points = List.of(
                new PointArretRequestDTO("Rabat", base, BigDecimal.ZERO),
                new PointArretRequestDTO("Casablanca", base.plusHours(1), BigDecimal.valueOf(40)),
                new PointArretRequestDTO("Rabat", base.plusHours(2), BigDecimal.valueOf(80))
        );
        TrajetRequestDTO dto = new TrajetRequestDTO(1L, 1L, 2, points);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(ValidationMetierException.class)
                .hasMessageContaining("apparaît plusieurs fois");
    }

    @Test
    void publier_conducteurInexistant_doitLeverRessourceNotFound() {
        TrajetRequestDTO dto = new TrajetRequestDTO(99L, 1L, 2, pointsValides());

        when(conducteurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trajetService.publier(dto))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessageContaining("Conducteur introuvable");
    }

    @Test
    void terminer_avantHeureDepart_doitLeverRegleMetier() {
        Trajet trajet = new Trajet();
        trajet.setId(1L);
        trajet.setConducteur(conducteur);
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(LocalDateTime.now().plusHours(5)); // départ dans le futur

        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> trajetService.terminer(1L, 1L))
                .isInstanceOf(com.croise.covoiturage.exception.RegleMetierException.class)
                .hasMessageContaining("avant son heure de départ");
    }

    @Test
    void terminer_conducteurNonProprietaire_doitLeverAccesInterdit() {
        Trajet trajet = new Trajet();
        trajet.setId(1L);
        trajet.setConducteur(conducteur); // appartient au conducteur 1L
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(LocalDateTime.now().minusHours(1));

        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> trajetService.terminer(1L, 99L)) // un autre conducteur
                .isInstanceOf(AccesInterditException.class);
    }
}