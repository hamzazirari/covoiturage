# 🚗 Plateforme de Covoiturage Multi-Étapes

API REST permettant à des conducteurs de publier des trajets avec plusieurs points d'arrêt, et à des passagers de rechercher et réserver des places sur tout ou partie du trajet.

## 📋 Contexte

Contrairement à une plateforme de covoiturage classique, un trajet ici n'est pas limité à un point de départ et un point d'arrivée : il peut comporter **plusieurs étapes intermédiaires**. Un passager peut réserver une place sur n'importe quel segment du parcours (ex: monter à Casablanca et descendre à Settat sur un trajet Rabat → Marrakech).

## 🛠️ Stack technique

| Catégorie | Technologie |
|---|---|
| Langage | Java 17 |
| Framework | Spring Boot 3 |
| Build | Maven |
| Base de données | PostgreSQL (via Docker Compose) |
| ORM | Spring Data JPA (Hibernate) |
| Mapping DTO | MapStruct |
| Validation | Bean Validation (Jakarta Validation) |
| Documentation API | Swagger / OpenAPI (springdoc) |
| Tests | JUnit 5, Mockito, AssertJ |
| Logs | SLF4J |

## 🏗️ Architecture

Le projet suit une architecture en couches classique :

```
Controller  →  Service (interface + implémentation)  →  Repository  →  Base de données
     ↓               ↓
   DTO  ←──────  Mapper (MapStruct)  ──────→  Entity
```

- **Controller** : reçoit les requêtes HTTP, valide les entrées (`@Valid`), délègue au Service
- **Service** : contient toute la logique métier et les règles de gestion
- **Repository** : accès aux données via Spring Data JPA, requêtes JPQL personnalisées
- **Entity** : représentation des tables en base
- **DTO (records)** : objets d'entrée/sortie de l'API, isolent les entités JPA du monde extérieur
- **Mapper** : conversion automatique Entity ↔ DTO via MapStruct
- **Exception** : exceptions métier personnalisées + gestion centralisée (`@RestControllerAdvice`)

## 📊 Modèle de données

Le projet comporte 6 entités principales :

- **Conducteur** : possède des véhicules et publie des trajets
- **Passager** : réserve des places sur des trajets
- **Vehicule** : appartient à un conducteur, utilisé pour publier des trajets
- **Trajet** : composé d'une liste ordonnée de points d'arrêt
- **PointArret** : une ville, une heure de passage, un prix cumulé depuis le départ
- **Reservation** : lie un passager à un segment d'un trajet (point de montée → point de descente)

Voir le diagramme de classes UML dans `/docs/diagramme-classes.png`.

### Concept clé : le calcul du prix d'un segment

Chaque point d'arrêt porte un **prix cumulé depuis le départ** (et non un prix "au tronçon"). Le prix d'un segment réservé se calcule ainsi :

```
Prix du segment = prix(point de descente) − prix(point de montée)
```

Exemple sur un trajet Rabat(0 DH) → Casablanca(40 DH) → Settat(70 DH) → Marrakech(120 DH) :
- Casablanca → Settat : 70 − 40 = **30 DH**
- Settat → Marrakech : 120 − 70 = **50 DH**

## 🚦 Statuts et cycle de vie

**Statut d'un trajet** (`StatutTrajet`) :

| Statut | Signification |
|---|---|
| `PUBLIE` | ouvert à la réservation |
| `COMPLET` | plus aucune place disponible (automatique) |
| `TERMINE` | voyage effectué (manuel, après l'heure de départ) |
| `ANNULE` | annulé par le conducteur (manuel, annule aussi les réservations) |

**Statut d'une réservation** (`StatutReservation`) : `CONFIRMEE`, `ANNULEE`

## ⚙️ Installation et lancement

### Prérequis

- Java 17+
- Maven 3.8+
- Docker et Docker Compose

### 1. Cloner le projet

```bash
git clone <URL_DU_DEPOT>
cd covoiturage
```

### 2. Lancer la base de données PostgreSQL

```bash
docker compose up -d
```

Vérifier que le conteneur tourne :
```bash
docker ps
```

### 3. Lancer l'application

```bash
mvn spring-boot:run
```

Ou depuis votre IDE, lancez directement la classe `CovoiturageApplication`.

L'application démarre sur `http://localhost:8080`.

### 4. Accéder à la documentation Swagger

```
http://localhost:8080/swagger-ui.html
```

Tous les endpoints de l'API y sont documentés et testables directement.

## 🔑 Configuration

Les identifiants de connexion à la base de données sont définis dans :
- `docker-compose.yml` (création de la base PostgreSQL)
- `src/main/resources/application.properties` (connexion Spring Boot)

| Variable | Valeur par défaut |
|---|---|
| Base de données | `covoiturage_db` |
| Utilisateur | `covoiturage_user` |
| Mot de passe | `covoiturage_pass` |
| Port PostgreSQL | `5432` |

## 📡 Principaux endpoints de l'API

### Conducteurs
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/conducteurs` | Créer un conducteur |
| GET | `/api/conducteurs/{id}` | Consulter un conducteur |
| GET | `/api/conducteurs` | Lister tous les conducteurs |
| PUT | `/api/conducteurs/{id}` | Modifier un conducteur |
| GET | `/api/conducteurs/recherche?motCle=` | Rechercher par nom ou email |

### Passagers
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/passagers` | Créer un passager |
| GET | `/api/passagers/{id}` | Consulter un passager |
| GET | `/api/passagers` | Lister tous les passagers |
| PUT | `/api/passagers/{id}` | Modifier un passager |
| GET | `/api/passagers/recherche?email=` | Rechercher par email |
| GET | `/api/passagers/{id}/reservations` | Historique des réservations |

### Véhicules
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/vehicules` | Ajouter un véhicule |
| GET | `/api/vehicules/{id}` | Consulter un véhicule |
| PUT | `/api/vehicules/{id}` | Modifier un véhicule |
| GET | `/api/vehicules/conducteur/{conducteurId}` | Véhicules d'un conducteur |
| DELETE | `/api/vehicules/{id}` | Supprimer (si aucun trajet associé) |

### Trajets
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/trajets` | Publier un trajet |
| GET | `/api/trajets/{id}` | Consulter le détail d'un trajet |
| GET | `/api/trajets/recherche?...` | Rechercher des trajets (paginé) |
| PATCH | `/api/trajets/{id}/terminer?conducteurId=` | Terminer un trajet |
| PATCH | `/api/trajets/{id}/annuler?conducteurId=` | Annuler un trajet |

**Paramètres de recherche** : `villeDepart`, `villeArrivee`, `date` (optionnel), `placesDemandees` (défaut 1), `prixMax` (optionnel), `page`, `taille`

### Réservations
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/reservations` | Réserver des places |
| DELETE | `/api/reservations/{id}?passagerId=` | Annuler une réservation |
| GET | `/api/reservations/passager/{passagerId}` | Réservations