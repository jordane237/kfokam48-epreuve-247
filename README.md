# kfokam48-epreuve-247

Épreuve finale fullstack KFOKAM48 — application **Présence & relecture par les pairs** pour la direction de la formation.

- **Backend :** Java 17 · Spring Boot · Maven (`mvnw` commité) · Flyway · H2 en mémoire
- **Frontend :** **Angular** — choisi parce qu'il impose nativement la séparation des couches exigée par le sujet (composants / services / couche d'appel API dédiée avec `HttpClient`, contrainte F3), avec un typage TypeScript de bout en bout
- **Base de données :** schéma versionné par migrations Flyway (V1 à V7), conforme au diagramme D2

## Démarrage (testé depuis un clone vierge — 3 commandes)

```bash
# 1. Backend — http://localhost:8080 (données de démonstration chargées par Flyway)
cd backend && ./mvnw spring-boot:run

# 2. Frontend — http://localhost:4200 (deuxième terminal)
cd frontend && npm install && npm start

# 3. Ouvrir http://localhost:4200
```

## Données de démonstration (jamais vide à l'ouverture)

- Promotion **L1 Informatique 2026** (id 1)
- 6 étudiants : Aline Mefire, Boris Tchoumi, Cynthia Ngo, David Ekwalla, Emma Nkoulou, Frank Mbarga
- Session **ouverte** de démonstration avec le code de présence **`DEMO24`** (expire 30 min après le démarrage)

**Test immédiat :** écran Étudiant → choisir un nom → saisir `DEMO24` → la présence apparaît dans le tableau du formateur (écran Formateur → « Charger le tableau »).

## Structure

```
docs/         CAHIER_DES_CHARGES.md · JOURNAL.md · backlog-issues.md · diagrammes/
api/          contrat.yaml (5 opérations imposées + 4 ajoutées)
backend/      Spring Boot 3 couches (controller/service/repository), Flyway, DTO, B1-B6
frontend/     Angular standalone, couche API dédiée (F1-F3), 3 écrans
```

## Documentation

| Document | Contenu |
|---|---|
| [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) | 10 sections : EF1–EF15, RG1–RG16, contradictions tranchées (Q10/Q15 → Q15) |
| [`docs/diagrammes/`](docs/diagrammes) | D1 cas d'utilisation · D2 classes · D3 séquence présence · D4 états de l'exercice (bonus) |
| [`docs/backlog-issues.md`](docs/backlog-issues.md) | Backlog (14 tickets, Must/Should) |
| [`api/contrat.yaml`](api/contrat.yaml) | Contrat OpenAPI — format d'erreur `{ code, message }` partout |
| [`docs/JOURNAL.md`](docs/JOURNAL.md) | Journal de bord, une entrée par étape |

## Tests

```bash
cd backend && ./mvnw test
```

11 tests, sans base locale (H2 + simulations) : unitaires sur les règles métier RG1/RG2/RG4/RG13/RG14, test d'intégration sur `POST /api/relectures/{id}` (B6).
