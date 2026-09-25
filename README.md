# kfokam48-epreuve-247

Épreuve finale fullstack KFOKAM48 — application **Présence & relecture par les pairs** pour la direction de la formation.

- **Backend :** Java 21 · Spring Boot · Maven (`mvnw` commité) · Flyway · PostgreSQL (H2 pour les tests)
- **Frontend :** **Angular** — choisi parce qu'il impose nativement la séparation des couches exigée par le sujet (composants / services / couche d'appel API dédiée avec `HttpClient`, contrainte F3), avec un typage TypeScript de bout en bout
- **Base de données :** schéma versionné par migrations Flyway, cohérent avec le diagramme D2

## Structure du dépôt

```
docs/         CAHIER_DES_CHARGES.md · JOURNAL.md · backlog-issues.md · diagrammes/
api/          contrat.yaml (5 opérations imposées + 3 ajoutées)
backend/      Spring Boot (étape 2 — pas encore créé, jalon analyse posé)
frontend/     Angular (étape 2 — pas encore créé)
```

## Documentation

| Document | Contenu |
|---|---|
| [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) | 10 sections : EF1–EF15, RG1–RG16, contradictions tranchées (Q10/Q15 → Q15) |
| [`docs/diagrammes/`](docs/diagrammes) | D1 cas d'utilisation · D2 classes · D3 séquence présence · D4 états de l'exercice (bonus) |
| [`docs/backlog-issues.md`](docs/backlog-issues.md) | 15 issues prêtes à créer (titre, critères, Must/Should, renvoi EFx/RGx) |
| [`api/contrat.yaml`](api/contrat.yaml) | Contrat OpenAPI — format d'erreur `{ code, message }` partout |
| [`docs/JOURNAL.md`](docs/JOURNAL.md) | Journal de bord, une entrée par étape |

## Démarrage

À venir à l'étape 2 (backend + frontend). Le schéma de base et les données de démonstration seront chargés au démarrage conformément à la contrainte « Démarrage » du sujet.
