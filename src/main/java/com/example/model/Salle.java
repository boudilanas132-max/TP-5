package com.example.model;

import javax.persistence.*;
import java.util.Set;

// Déclarer la classe comme une entité JPA
@Entity
// Définir le nom de la table dans la base de données
@Table(name = "salles")
public class Salle {

    // Clé primaire générée automatiquement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom obligatoire et unique pour chaque salle
    @Column(nullable = false, unique = true)
    private String nom;

    // Capacité obligatoire de la salle
    @Column(nullable = false)
    private int capacite;

    // Une salle peut posséder plusieurs réservations
    // mappedBy indique que la relation est gérée par l'attribut "salle"
    // dans la classe Reservation
    @OneToMany(mappedBy = "salle",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    private Set<Reservation> reservations;

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }
}
