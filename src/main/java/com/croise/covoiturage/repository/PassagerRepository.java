package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassagerRepository extends JpaRepository<Passager, Long> {

    Optional<Passager> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Passager> findByEmailContainingIgnoreCase(String email);
}