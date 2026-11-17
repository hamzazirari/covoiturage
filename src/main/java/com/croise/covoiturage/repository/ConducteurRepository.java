package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    Optional<Conducteur> findByEmail(String email);

    Optional<Conducteur> findByNumeroPermis(String numeroPermis);

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    // Recherche par nom OU email (contient le texte, insensible à la casse)
    java.util.List<Conducteur> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nom, String email);
}