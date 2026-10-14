package com.example.repository;

import com.example.model.Salle;
import com.example.model.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalleRepositoryImpl implements SalleRepository {

    private EntityManager entityManager;

    // Constructeur : recevoir l'EntityManager depuis App.java
    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // rechercher les salles disponibles avec JPQL
    @Override
    public List<Salle> findAvailableRooms(
            LocalDateTime debut,
            LocalDateTime fin) {

        verifierDates(debut, fin);

        String jpql =
                "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r WHERE " +
                "(:debut < r.fin AND :fin > r.debut)" +
                ")";

        TypedQuery<Salle> query =
                entityManager.createQuery(jpql, Salle.class);

        query.setParameter("debut", debut);
        query.setParameter("fin", fin);

        return query.getResultList();
    }

    //  recherche multicritere avec pagination
    @Override
    public List<Salle> searchRooms(
            String nom,
            Integer capaciteMin,
            LocalDateTime debut,
            LocalDateTime fin,
            int page,
            int size) {

        verifierDates(debut, fin);

        if (page < 1 || size < 1) {
            throw new IllegalArgumentException(
                    "La page et la taille doivent etre positives.");
        }

        if (capaciteMin != null && capaciteMin < 0) {
            throw new IllegalArgumentException(
                    "La capacite minimale ne peut pas etre negative.");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> predicates = construireCriteres(
                cb, cq, salle, nom, capaciteMin, debut, fin
        );

        cq.select(salle)
          .where(cb.and(predicates.toArray(new Predicate[0])))
          .orderBy(cb.asc(salle.<Long>get("id")));

        TypedQuery<Salle> query = entityManager.createQuery(cq);

        // Appliquer la pagination
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    // Compter les salles correspondant aux criteres
    @Override
    public long countRooms(
            String nom,
            Integer capaciteMin,
            LocalDateTime debut,
            LocalDateTime fin) {

        verifierDates(debut, fin);

        if (capaciteMin != null && capaciteMin < 0) {
            throw new IllegalArgumentException(
                    "La capacite minimale ne peut pas etre negative.");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> predicates = construireCriteres(
                cb, cq, salle, nom, capaciteMin, debut, fin
        );

        cq.select(cb.count(salle))
          .where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(cq).getSingleResult();
    }

    // Construire les criteres communs aux deux requetes
    private List<Predicate> construireCriteres(
            CriteriaBuilder cb,
            CriteriaQuery<?> cq,
            Root<Salle> salle,
            String nom,
            Integer capaciteMin,
            LocalDateTime debut,
            LocalDateTime fin) {

        List<Predicate> predicates = new ArrayList<>();

        // Recherche par nom, sans distinguer majuscules/minuscules
        if (nom != null && !nom.trim().isEmpty()) {
            predicates.add(
                    cb.like(
                            cb.lower(salle.<String>get("nom")),
                            "%" + nom.trim().toLowerCase() + "%"
                    )
            );
        }

        // Capacite minimale
        if (capaciteMin != null) {
            predicates.add(
                    cb.ge(salle.<Integer>get("capacite"), capaciteMin)
            );
        }

        // Disponibilite pendant le creneau demande
        if (debut != null && fin != null) {
            Subquery<Long> subquery = cq.subquery(Long.class);
            Root<Reservation> reservation =
                    subquery.from(Reservation.class);

            subquery.select(
                    reservation.<Salle>get("salle").<Long>get("id")
            );

            subquery.where(
                    cb.and(
                            cb.lessThan(
                                    reservation.<LocalDateTime>get("debut"),
                                    fin
                            ),
                            cb.greaterThan(
                                    reservation.<LocalDateTime>get("fin"),
                                    debut
                            )
                    )
            );

            predicates.add(
                    cb.not(salle.<Long>get("id").in(subquery))
            );
        }

        return predicates;
    }

    // Verifier la validite des dates
    private void verifierDates(
            LocalDateTime debut,
            LocalDateTime fin) {

        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException(
                    "Les deux dates doivent etre renseignees ensemble.");
        }

        if (debut != null && !debut.isBefore(fin)) {
            throw new IllegalArgumentException(
                    "Le debut doit etre anterieur a la fin.");
        }
    }
}
