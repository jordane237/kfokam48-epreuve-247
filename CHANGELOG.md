# CHANGELOG

Toutes les évolutions notables de ce projet sont documentées ici.
Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) —
historique détaillé : `git log` et [docs/JOURNAL.md](docs/JOURNAL.md).

## [1.0.0] — 25/09 (étape 4)

### Ajouté (v0.1 — étape 2, tickets T1–T11, PR #15 à #26)

- Squelette technique : backend Spring Boot 3 couches (controller/service/repository), Flyway, erreurs `{ code, message }` (B3, B4) ; frontend **Angular 20** standalone avec couche d'appel API dédiée (F1, F3) — PR #15
- `POST /api/sessions` : ouverture de session, code court sans 0/O/1/I, expiration 15 min (EF1, RG1) — PR #16
- `POST /api/presences` : marquage avec erreurs 400/409/410 dans l'ordre D3, blocage 2 min après 5 échecs (EF2/EF3/EF4, RG2, RG3, RG4) — PR #17
- `POST /api/exercices` : dépôt de lien, 400 LIEN_INVALIDE, 409 EXERCICE_DEJA_DEPOSE (EF5, RG5) — PR #18
- Assignation automatique du relecteur au hasard parmi les présents hors auteur, repli `en_attente_relecteur` sans erreur (EF8, RG13, RG14) — PR #19
- `POST /api/relectures/{id}` : note entière 0–20 + commentaire, 403 auto-relecture, 409 définitive (EF9/EF10, RG7, RG8, RG10) — PR #20
- `GET /api/tableau?promotionId=` : suivi par étudiant, moyenne calculée côté API (EF13, RG15, Q16) — PR #21
- Écrans Formateur / Étudiant / Relecteur (F2) — PR #22, #23, #24
- Design system global (tokens, cartes, boutons, champs, tableaux, badges) — PR #26
- Tests B6 (unitaires RG1/RG2/RG4/RG13, intégration MockMvc) + données de démonstration au démarrage (seed V7, `DEMO24`), CORS — PR #25
- Opérations ajoutées au contrat : `GET /api/etudiants` (Q1), `GET /api/relectures/en-attente` (Q11), `GET /api/exercices/{id}/retour` (Q8), `PUT /api/exercices/{id}` (EF6/RG6 — PR #30), `POST /api/sessions/{id}/cloture` (EF7/RG12 — PR #29), `POST /api/sessions/{id}/presences-manuelles` (EF12/RG11 — PR #28)
- Enrichissement des seeds : 30 étudiants, session passée riche `PAST99`, V8 (PR #31)

### Corrigé

- **Présences concurrentes perdues sur `POST /api/presences`** (issue #33, PR #34) : deux marquages simultanés pouvaient aboutir à un 500 au lieu de 201/409 (course TOCTOU : vérification RG2 puis INSERT ; H2 2.4.240 souffrait en plus du bug amont #4302 « Check constraint invalid » sous inserts simultanés). Correctif : INSERT atomique et indépendant, la contrainte unique `(session_id, etudiant_id)` sert de filet de sécurité — le perdant reçoit `409 DEJA_PRESENT` ; H2 passée en 2.5.250 ; les 500 inattendues sont désormais loguées. Prouvé par test concurrent (rouge avant correctif, vert après).
- README : procédure complète si le port 8080 est déjà occupé (PR #32)

### Changé

- **Double relecture par exercice** (issues #35–#38, PR #40 — changement de besoin de l'étape 3) :
  - chaque exercice est relu par **deux relecteurs distincts** tirés au hasard parmi les présents hors auteur (un seul si un seul candidat ; aucun → `en_attente_relecteur`) ;
  - la note affichée à l'étudiant est la **moyenne des relectures rendues**, avec mention **PROVISOIRE** tant qu'une seule est rendue (nouveau statut d'exercice `relu_partiel`) ;
  - chaque relecture **individuelle** reste définitive (RG10 inchangé) ; soumission par affectation via `relecteurId` optionnel dans le corps de `POST /api/relectures/{id}` (chemins/verbes/codes imposés inchangés, B2) ;
  - migration V9 : `unique(exercice_id)` → `unique(exercice_id, relecteur_id)`, sans perte de données ;
  - ⚠️ **casse la règle d'origine RG16 issue de Q6 (« un seul relecteur »)** — rupture assumée, documentée au cahier des charges §7. (La demande client parlait de « RG3 » ; dans ce CDC la règle « un seul relecteur » est RG16, RG3 restant le blocage anti brute-force.)
- Tableau du formateur : moyenne par exercice (moyenne des relectures rendues) puis agrégation — toujours calculée côté API (F3)
- Hors périmètre assumé à cette occasion : badge « provisoire » dans le tableau du formateur (issue #39, Could) — la mention reste sur l'écran étudiant
