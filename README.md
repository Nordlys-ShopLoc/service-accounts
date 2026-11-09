# service-accounts

Microservice ShopLoc chargé des comptes, de l'authentification et des cartes de fidélité.

## Rôle

Module M1 du cahier des charges : inscription en ligne ou en boutique, authentification, carte de fidélité avec porte-monnaie rechargeable (plafonné à 100 €, règle RM01) et gestion des comptes.

## Données

| Table | Contenu |
|---|---|
| `users` | comptes clients, commerçants et administrateurs |
| `loyalty_cards` | cartes de fidélité : QR code, solde, statut |

Les migrations Flyway sont dans `src/main/resources/db/migration`. Un schéma déjà appliqué ne se modifie jamais : toute évolution passe par un nouveau fichier `V<n>__description.sql`.

## Architecture
fr.nordlys.shoploc.accounts
├── api/ entrée HTTP : controllers, DTO
├── domain/ métier pur, sans framework
│ ├── model/ Account, LoyaltyCard…
│ ├── port/ contrats de persistance
│ └── service/ logique métier
└── infrastructure/persistence/ accès à PostgreSQL
├── entity/
├── repository/
└── adapter/ implémentations des ports

## Lancer le service en local

Prérequis : Java 21 et Docker Desktop démarré.

```bash
./mvnw spring-boot:test-run
```

Le service démarre sur http://localhost:8082 avec une base PostgreSQL temporaire, créée automatiquement dans Docker.

## Tests

```bash
./mvnw clean verify
```

## Image Docker

```bash
docker build -t service-accounts:local .
```

## Configuration

| Variable | Défaut |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/accounts` |
| `SPRING_DATASOURCE_USERNAME` | `accounts` |
| `SPRING_DATASOURCE_PASSWORD` | `accounts` |
| `SERVER_PORT` | `8082` |

## Contribuer

Une branche `feature/...` par user story, créée depuis `develop`. Commits au format Conventional Commits, en anglais. Pull request vers `develop`, relue par un autre membre, avec la CI au vert.
