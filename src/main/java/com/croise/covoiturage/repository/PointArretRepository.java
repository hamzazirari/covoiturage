package com.croise.covoiturage.repository;

import com.croise.covoiturage.entity.PointArret;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointArretRepository extends JpaRepository<PointArret, Long> {
}