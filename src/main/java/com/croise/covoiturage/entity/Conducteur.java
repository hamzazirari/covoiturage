package com.croise.covoiturage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conducteurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conducteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(name = "numero_permis", nullable = false, unique = true)
    private String numeroPermis;

    @OneToMany(mappedBy = "conducteur", cascade = CascadeType.ALL)
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "conducteur")
    private List<Trajet> trajets = new ArrayList<>();
}