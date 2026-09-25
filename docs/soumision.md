# Soumission — Épreuve finale fullstack KFOKAM48

**Dépôt :** https://github.com/jordane237/kfokam48-epreuve-247

**Application :** Présence & relecture par les pairs (Spring Boot 4 + Angular 20, H2 en mémoire / PostgreSQL cible, Flyway)

---

## Démarrage (3 commandes, testées depuis un clone vierge)

```bash
# 1. Backend — http://localhost:8080 (données de démonstration chargées par Flyway)
cd backend && ./mvnw spring-boot:run

# 2. Frontend — http://localhost:4200 (deuxième terminal)
cd frontend && npm install && npm start

# 3. Ouvrir http://localhost:4200
```

Si le port 8080 est occupé (cas documenté dans le README) : `SERVER_PORT=8081 ./mvnw spring-boot:run` + bascule de `apiUrl` dans `frontend/src/environments/environment.ts`.

## Données de démonstration (jamais vide à l'ouverture)

- Promotion **L1 Informatique 2026**, **30 étudiants**
- Session clôturée `PAST99` : 30 présences (dont 10 ajoutées par le formateur), 8 exercices, relectures rendues et une en attente — le tableau du formateur est déjà riche
- Session ouverte **`DEMO24`** (expire 30 min après le démarrage), 2 étudiants déjà présents
- Démonstration **double relecture** : exercices avec deux notes (définitives), un exercice en **note PROVISOIRE** (une seule relecture rendue), une seconde affectation en attente (visible dans l'écran relecteur)

## État des étapes

| Étape | Jalons / livrables | État |
|---|---|---|
| 1 — Analyse & conception | `[JALON] analyse` · CDC 10 sections (EF1–EF15, RG1–RG16), D1–D4 (Mermaid), `api/contrat.yaml`, backlog | ✅ |
| 2 — Première version | `[JALON] v0.1` · 11 tickets Must livrés, 3 écrans Angular, tests verts | ✅ |
| 3 — Conduite du changement | Bug concurrence (#33 → PR #34) + changement de besoin double relecture (#35–#39, PR dédiée), journal « Étape 3 » (fait / bloqué / vérification IA) | ✅ |
| 4 — Livrabilité | README complet, port alternatif documenté, données de démo, journal horodaté | ✅ |

## Suivi (issues & PR)

- **14 tickets initiaux** (#1–#14) fermés par PR #15–#26 avec `Closes #n` · **#27** documentation (PR #31–#32)
- **Étape 3 :** #33 bug concurrence (PR #34, test rouge→vert démontré) · #35–#38 double relecture (PR dédiée) · #39 hors périmètre (sacrifice documenté au journal)
- **17 PR mergées**, commits atomiques, branches dédiées, auteur unique

## Qualité

- **Tests : `cd backend && ./mvnw test` → 21/21 verts** (unitaires RG1/RG2/RG4/RG13/RG14/RG16, intégrations présence 400/409/410, relecture, concurrence, double relecture)
- Build frontend : `cd frontend && npx ng build` → vert
- Migrations Flyway **V1–V9**, `ddl-auto=none`, aucune migration poussée jamais modifiée
- Contrat OpenAPI : 5 opérations imposées inchangées (B2), format d'erreur `{ code, message }` partout (B4), moyenne calculée côté API (F3)
- Séparation des couches : contrôleurs → services → repositories (B3), DTO uniquement

## Contenu

```
docs/         CAHIER_DES_CHARGES.md · JOURNAL.md · backlog-issues.md · diagrammes/ D1–D4
api/          contrat.yaml (5 opérations imposées + ajoutées, double relecture documentée)
backend/      Spring Boot, 3 couches, Flyway V1–V9, 21 tests
frontend/     Angular standalone, couche API dédiée (F3), 3 écrans
```
