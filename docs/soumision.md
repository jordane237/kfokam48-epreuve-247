# Soumission — Épreuve finale fullstack KFOKAM48

**Dépôt :** https://github.com/jordane237/kfokam48-epreuve-247
**Application :** Présence & relecture par les pairs — Spring Boot 4 + Angular 20, H2 en mémoire (PostgreSQL cible), Flyway V1–V9

---

## Démarrage (3 commandes, testées depuis un clone vierge)

```bash
# 1. Backend — http://localhost:8080 (données de démonstration chargées par Flyway)
cd backend && ./mvnw spring-boot:run

# 2. Frontend — http://localhost:4200 (deuxième terminal)
cd frontend && npm install && npm start

# 3. Ouvrir http://localhost:4200
```

Si le port 8080 est occupé (cas rencontré, documenté dans le README) : `SERVER_PORT=8081 ./mvnw spring-boot:run` + bascule de `apiUrl` dans `frontend/src/environments/environment.ts`.

## Scénario de vérification rapide (~5 minutes)

1. **Formateur** : « Charger le tableau » → 30 étudiants, présences, exercices, moyennes déjà renseignés (moyennes calculées côté API, F3)
2. **Étudiant** : choisir un nom → code **`DEMO24`** → présence enregistrée (visible dans le tableau)
3. **Relecteur** : étudiant 10 → une relecture en attente → rendre note + commentaire
4. **Étudiant (retour)** : exercice 8 → note **PROVISOIRE** (une seule relecture rendue) ; exercice 1 → note **définitive** (moyenne des deux relectures)
5. **Formateur** : ouvrir une session → code affiché ; clôturer → dépôt et présence manuelle refusés (409)

## Données de démonstration (jamais vide à l'ouverture)

- Promotion **L1 Informatique 2026**, **30 étudiants**
- Session clôturée `PAST99` : 30 présences (dont 10 ajoutées par le formateur, Q14), 8 exercices, 14 relectures dont 13 rendues et 2 affectations en attente — tableau déjà riche
- Session ouverte **`DEMO24`** (expire 30 min après le démarrage), 2 étudiants déjà présents
- Démonstration **double relecture** : exercices 1–6 avec deux notes (définitives), exercice 7 en **note PROVISOIRE** (`relu_partiel`), exercice 8 avec une affectation en attente (écran relecteur, étudiant 10)

## État des étapes

| Étape | Jalons / livrables | État |
|---|---|---|
| 1 — Analyse & conception | `[JALON] analyse` · CDC 10 sections (EF1–EF15, RG1–RG16), D1–D4 (Mermaid), `api/contrat.yaml`, backlog 15 tickets | ✅ |
| 2 — Première version | `[JALON] v0.1` · 11 tickets Must livrés, 3 écrans Angular, tests verts | ✅ |
| 3 — Conduite du changement | **A)** Bug concurrence : #33 → PR #34 (test rouge→vert démontré). **B)** Changement de besoin double relecture : analyse d'abord (commit docs isolé) → #35–#39 → PR #40 mergée. Journal « Étape 3 » complet (fait / bloqué / vérification IA) | ✅ |
| 4 — Livrabilité | README (3 commandes + port alternatif), données de démo riches, journal horodaté, fichier de soumission | ✅ |

## Suivi (issues & PR)

- **14 tickets initiaux** (#1–#14) fermés par PR #15–#26 avec `Closes #n` · **#27** documentation (PR #31–#32)
- **Étape 3 — A :** #33 bug concurrence (PR #34) · **B :** #35–#38 double relecture (PR #40, fermées à la livraison) · **#39 volontairement ouverte** (Could, hors périmètre — sacrifice documenté au journal)
- **19 PR mergées**, commits atomiques, une branche par sujet, bug fix et changement de besoin **jamais mélangés** (branches `fix/concurrence-presence` et `feature/double-relecture` séparées)

## Choix assumés à connaître (détail au journal et au CDC §7)

- **Q10/Q15 tranché Q15** : chaque relecture individuelle reste définitive (409 RELECTURE_DEJA_RENDUE) — la double relecture ne change rien à RG10, seule la note *affichée* devient une moyenne (provisoire si une seule rendue)
- **Rupture assumée de RG16 (Q6)** : deux relecteurs par exercice — changement de besoin client de l'étape 3, migration V9 en `unique(exercice_id, relecteur_id)`, sans perte de données
- **Contrat B2 intact** : `POST /api/relectures/{id}` garde chemin/verbes/codes ; `relecteurId` optionnel dans le corps désigne l'affectation du relecteur (requis dès deux affectations → 400 RELECTEUR_REQUIS)
- **Bug de concurrence corrigé** : INSERT atomique + contrainte unique en filet de sécurité → 409 au lieu de 500 ; H2 passée en 2.5.250 (bug amont #4302)
- **Sacrifice explicite** : #39 (badge provisoire dans le tableau formateur) sortie du périmètre pour tenir le délai — la mention reste sur l'écran étudiant

## Qualité

- **Tests : `cd backend && ./mvnw test` → 21/21 verts** (unitaires RG1/RG2/RG4/RG13/RG14/RG16, intégrations présence 400/409/410, relecture, concurrence, double relecture)
- Build frontend : `cd frontend && npx ng build` → vert
- Migrations Flyway **V1–V9**, `ddl-auto=none`, aucune migration poussée jamais modifiée, compatibles données existantes
- Format d'erreur `{ code, message }` partout (B4), moyenne calculée côté API (F3), contrôleurs → services → repositories (B3), DTO uniquement
- Les 5 opérations imposées du contrat respectées à la lettre depuis l'étape 1 (B2)

## Contenu

```
docs/         CAHIER_DES_CHARGES.md · JOURNAL.md (étapes 1-2-3) · backlog-issues.md · diagrammes/ D1–D4 · soumision.md
api/          contrat.yaml (5 opérations imposées + ajoutées, double relecture documentée)
backend/      Spring Boot, 3 couches, Flyway V1–V9, 21 tests, fix concurrence
frontend/     Angular standalone, couche API dédiée (F3), 3 écrans (badge provisoire)
```
