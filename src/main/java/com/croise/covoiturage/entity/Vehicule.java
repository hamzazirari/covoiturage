package com.croise.covoiturage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Column(nullable = false)
    private String couleur;

    @Column(nullable = false)
    private int capacite;

    @ManyToOne
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @OneToMany(mappedBy = "vehicule")
    private List<Trajet> trajets = new ArrayList<>();
}