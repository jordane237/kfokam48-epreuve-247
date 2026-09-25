# kfokam48-epreuve-247

Épreuve finale fullstack KFOKAM48 — application **Présence & relecture par les pairs** pour la direction de la formation.

- **Backend :** Java 17 · Spring Boot · Maven (`mvnw` commité) · Flyway · H2 en mémoire
- **Frontend :** **Angular** — choisi parce qu'il impose nativement la séparation des couches exigée par le sujet (composants / services / couche d'appel API dédiée avec `HttpClient`, contrainte F3), avec un typage TypeScript de bout en bout
- **Base de données :** schéma versionné par migrations Flyway (V1 à V8), conforme au diagramme D2

## Démarrage (testé depuis un clone vierge — 3 commandes)

```bash
# 1. Backend — http://localhost:8080 (données de démonstration chargées par Flyway)
cd backend && ./mvnw spring-boot:run

# Si le port 8080 est déjà occupé (ex. Keycloak lancé sur le poste) : voir la section juste en dessous.

# 2. Frontend — http://localhost:4200 (deuxième terminal)
cd frontend && npm install && npm start

# 3. Ouvrir http://localhost:4200
```

### Si le port 8080 est déjà occupé (cas fréquent : Keycloak lancé en local)

Le backend Spring Boot ne peut pas démarrer si un autre service écoute déjà sur 8080
(erreur « Port 8080 was already in use »). Procédure de contournement, testée :

```bash
# 1. Démarrer le backend sur un port libre :
cd backend && SERVER_PORT=8081 ./mvnw spring-boot:run

# 2. Vérifier qu'il répond :
curl http://localhost:8081/api/health    # {"status":"OK","application":"kfokam48-presence"}

# 3. Pointer le frontend vers ce port : dans frontend/src/environments/environment.ts,
#    remplacer apiUrl: 'http://localhost:8080' par apiUrl: 'http://localhost:8081'
#    puis (re)lancer npm start.
```

Aucun autre réglage n'est nécessaire : la configuration CORS du backend autorise
déjà l'origine http://localhost:4200, quel que soit le port du backend.

## Données de démonstration (jamais vide à l'ouverture)

- Promotion **L1 Informatique 2026** (id 1)
- **30 étudiants** (liste de choix de la connexion, Q1)
- Session **passée et clôturée** `PAST99` : 30 présences (20 étudiantes + 10 ajoutées par le formateur, RG11), 8 exercices déposés, **7 notes rendues** (moyennes visibles dans le tableau) et **1 relecture en attente** (étudiant 9, écran Relecteur)
- Session **ouverte** de démonstration avec le code de présence **`DEMO24`** (expire 30 min après le démarrage) — 2 étudiants y sont déjà présents, les 28 autres peuvent marquer en direct

**Test immédiat :** écran Formateur → « Charger le tableau » (le tableau est déjà riche) ; puis écran Étudiant → choisir un nom → saisir `DEMO24` ; ou écran Relecteur → étudiant 9 → rendre la relecture en attente.

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
| [`docs/backlog-issues.md`](docs/backlog-issues.md) | Backlog (15 tickets, Must/Should) |
| [`api/contrat.yaml`](api/contrat.yaml) | Contrat OpenAPI — format d'erreur `{ code, message }` partout |
| [`docs/JOURNAL.md`](docs/JOURNAL.md) | Journal de bord, une entrée par étape |

## Tests

```bash
cd backend && ./mvnw test
```

15 tests, sans base locale (H2 + simulations) : unitaires sur les règles métier RG1/RG2/RG4/RG13/RG14, tests d'intégration sur `POST /api/presences` (201 nominal, 400 CODE_INCONNU, 409 DEJA_PRESENT, 410 CODE_EXPIRE) et sur `POST /api/relectures/{id}` (B6).
