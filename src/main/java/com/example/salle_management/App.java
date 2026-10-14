package com.example.salle_management;

import com.example.model.Salle;
import com.example.model.Reservation;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            // CREER LA CONNEXION JPA
            emf = Persistence.createEntityManagerFactory("SallePU");
            em = emf.createEntityManager();

            // CREER LE REPOSITORY ET LE SERVICE
            SalleRepository repository = new SalleRepositoryImpl(em);
            SalleService service = new SalleServiceImpl(repository);

            // INSERER LES SALLES ET LA RESERVATION
            em.getTransaction().begin();

            Salle salle1 = new Salle();
            salle1.setNom("Salle A");
            salle1.setCapacite(20);
            em.persist(salle1);

            Salle salle2 = new Salle();
            salle2.setNom("Salle B");
            salle2.setCapacite(30);
            em.persist(salle2);

            Reservation reservation = new Reservation();
            reservation.setSalle(salle1);
            reservation.setDebut(
                LocalDateTime.of(2026, 10, 15, 10, 0)
            );
            reservation.setFin(
                LocalDateTime.of(2026, 10, 15, 11, 0)
            );
            em.persist(reservation);

            em.getTransaction().commit();

            // RECHERCHE AVEC JPQL
            System.out.println("\n--- Recherche avec JPQL ---");

            List<Salle> sallesDisponibles =
                repository.findAvailableRooms(
                    LocalDateTime.of(2026, 10, 15, 10, 0),
                    LocalDateTime.of(2026, 10, 15, 12, 0)
                );

            afficherSalles(sallesDisponibles);

            // RECHERCHE MULTICRITERE
            System.out.println("\n--- Recherche multicritere ---");

            List<Salle> resultats = repository.searchRooms(
                "Salle",
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0),
                1,
                10
            );

            afficherSalles(resultats);

            // PAGINATION VIA LE REPOSITORY
            System.out.println("\n--- Pagination ---");

            int pageNumber = 1;
            int pageSize = 1;

            List<Salle> sallesPage = repository.searchRooms(
                "Salle", null, null, null, pageNumber, pageSize
            );

            long totalElements = repository.countRooms(
                "Salle", null, null, null
            );

            int totalPages =
                (int) Math.ceil((double) totalElements / pageSize);

            System.out.println("Nombre total de salles : " + totalElements);
            System.out.println("Nombre total de pages : " + totalPages);
            System.out.println("Page demandee : " + pageNumber);

            afficherSalles(sallesPage);

            // TEST DU REPOSITORY JPA
            System.out.println("\n--- Repository JPA ---");

            List<Salle> resultatsRepository = repository.searchRooms(
                "Salle",
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0),
                1,
                10
            );

            long nombreTotalRepository = repository.countRooms(
                "Salle",
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0)
            );

            System.out.println(
                "Nombre de salles correspondant aux criteres : "
                + nombreTotalRepository
            );

            afficherSalles(resultatsRepository);

            // TEST DU SERVICE METIER
            System.out.println("\n--- Service metier ---");

            List<Salle> sallesService = service.getAvailableRooms(
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0)
            );

            System.out.println("\nSalles disponibles via le service :");
            afficherSalles(sallesService);

            List<Salle> resultatsService = service.searchRooms(
                "Salle",
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0),
                1,
                10
            );

            System.out.println("\nRecherche multicritere via le service :");
            afficherSalles(resultatsService);

            long nombreTotalService = service.countRooms(
                "Salle",
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 15, 12, 0)
            );

            System.out.println(
                "Nombre total via le service : " + nombreTotalService
            );

            //  TEST DE LA CLASSE PaginationResult
            System.out.println("\n--- PaginationResult ---");

            PaginationResult<Salle> pagination =
                new PaginationResult<>(
                    sallesPage,
                    pageNumber,
                    pageSize,
                    totalElements
                );

            System.out.println(
                "Page actuelle : " + pagination.getCurrentPage()
            );

            System.out.println(
                "Taille de la page : " + pagination.getPageSize()
            );

            System.out.println(
                "Nombre total de salles : " + pagination.getTotalItems()
            );

            System.out.println(
                "Nombre total de pages : " + pagination.getTotalPages()
            );

            System.out.println("\nSalles de la page demandee :");
            afficherSalles(pagination.getItems());

        } catch (Exception e) {

            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();

        } finally {

            // FERMER LES RESSOURCES
            if (em != null && em.isOpen()) {
                em.close();
            }

            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }

    // METHODE POUR AFFICHER LES SALLES
    private static void afficherSalles(List<Salle> salles) {

        if (salles == null || salles.isEmpty()) {
            System.out.println("Aucune salle trouvee.");
            return;
        }

        for (Salle salle : salles) {
            System.out.println(
                "ID : " + salle.getId()
                + " | Nom : " + salle.getNom()
                + " | Capacite : " + salle.getCapacite()
            );
        }
    }
}
