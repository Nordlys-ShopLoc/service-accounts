# service-accounts

Microservice ShopLoc chargé des comptes, de l'authentification et des cartes de fidélité.

## Rôle

Module M1 du cahier des charges : inscription en ligne ou en boutique, authentification, carte de fidélité avec porte-monnaie rechargeable (plafonné à 100 €, règle RM01) et gestion des comptes.

## Données

| Table | Contenu |
|---|---|
| `users` | comptes clients, commerçants, agents de la collectivité, policiers municipaux et administrateurs |
| `loyalty_cards` | cartes de fidélité : QR code, solde, statut |

Les migrations Flyway sont dans `src/main/resources/db/migration`. Un schéma déjà appliqué ne se modifie jamais : toute évolution passe par un nouveau fichier `V<n>__description.sql`.

## API

Accessible à travers la passerelle `api-gateway`.

| Méthode | Chemin | Rôle |
|---|---|---|
| POST | `/api/v1/auth/login` | connexion : renvoie un jeton JWT valable une heure |

Le jeton contient l'identifiant et le rôle de l'utilisateur. Il est signé avec la clé `JWT_SECRET`, que la gateway utilise pour le vérifier : c'est elle qui contrôle l'accès à toutes les routes (R2 §1.2). Ce service n'est jamais exposé directement.

## Architecture

```
fr.nordlys.shoploc.accounts
├── api/                         entrée HTTP
│   ├── controller/              routes et traduction des erreurs en codes HTTP
│   └── dto/                     objets JSON échangés
├── domain/                      métier pur, sans framework
│   ├── exception/               erreurs métier
│   ├── model/                   Account, LoyaltyCard…
│   ├── port/                    contrats vers l'extérieur : base, chiffrement, jeton
│   └── service/                 logique métier
└── infrastructure/              implémentations techniques
    ├── config/                  déclaration des services et sécurité
    ├── persistence/             accès à PostgreSQL : entity, repository, adapter
    └── security/                chiffrement BCrypt et émission des JWT
```

## Lancer le service en local

Prérequis : Java 21 et Docker Desktop démarré.

```bash
./mvnw spring-boot:test-run
```

Le service démarre sur http://localhost:8082 avec une base PostgreSQL temporaire, créée automatiquement dans Docker, et une clé de signature de test.

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
| `JWT_SECRET` | aucun : obligatoire, 32 caractères minimum |

## Contribuer

Une branche `feature/...` par user story, créée depuis `develop`. Commits au format Conventional Commits, en anglais. Pull request vers `develop`, relue par un autre membre, avec la CI au vert.
