package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {


}