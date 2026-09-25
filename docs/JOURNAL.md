# Journal de bord — kfokam48-epreuve-247

> Une entrée par étape. Trois lignes : ce que je viens de faire, ce qui m'a bloqué et combien de temps, ce que j'ai demandé à l'IA et comment j'ai vérifié sa réponse.

---

## Étape 1 — Analyse et conception (≈ 11 h 45 – 12 h 45, 25/09)

Fait : cahier des charges complet (15 EF avec critères vérifiables, 16 RG référencées aux Qx), 4 diagrammes Mermaid (D1 cas d'utilisation, D2 classes, D3 séquence présence, D4 états de l'exercice — bonus), 15 issues rédigées dans `docs/backlog-issues.md` (gh indisponible), contrat `api/contrat.yaml` complété (5 opérations imposées + 3 ajoutées), jalon `[JALON] analyse` posé avant tout commit de code.

Bloqué : 10 min — `gh` absent du poste, issues non créées directement sur GitHub, je les créerai par copier-coller depuis `docs/backlog-issues.md`. 15 min sur la contradiction Q10/Q15 avant de comprendre que le `409 RELECTURE_DEJA_RENDUE` du contrat interdisait déjà toute correction : j'ai tranché Q15 (RG10), RG9 (Q10) marquée abandonnée.

IA : je lui ai fourni CLIENT.md et mes 6 arbitrages imposés, et demandé cahier des charges, diagrammes, contrat et backlog. Vérification : j'ai relu chaque code HTTP du contrat contre l'Annexe B du sujet (5 opérations inchangées, format d'erreur `{ code, message }` partout), contrôlé que D2 correspond aux futures tables Flyway (unicités RG2/RG5, nullable `cloture_at` RG12) et que D3 renvoie exactement 400/409/410/429 dans l'ordre blocage → inconnu → expiré → déjà présent ; relu les 15 issues pour n'en garder aucune qui soit une tâche technique plutôt qu'un résultat utilisateur.

---

## Étape 2 — Première version (v0.1) (≈ 13 h 30 – 15 h 25, 25/09)

Fait : les 11 tickets Must implémentés, chacun sur sa branche, une PR par ticket fermant l'issue (`Closes #n`) : T1 squelette Spring Boot 4 + Angular 20, T2 POST /api/sessions, T3 présences avec blocage RG4, T4 dépôt d'exercice, T5 assignation aléatoire du relecteur, T6 relecture définitive RG10, T7 tableau du formateur, T8-T10 les trois écrans, T11 seed + CORS + README. 11 tests verts (unitaires RG1/RG2/RG4/RG13/RG14 + intégration MockMvc), chaque endpoint vérifié par curl contre le contrat.

Bloqué : 20 min — port 8080 occupé par un autre processus, tests basculés sur 8081 via SERVER_PORT. 15 min sur un test RG2 qui ne passait pas : id d'entité non persistée = null, `anyLong()` de Mockito ne matche pas, corrigé avec `any()`. 10 min : Spring Boot 4 a déplacé @AutoConfigureMockMvc dans org.springframework.boot.webmvc.test.autoconfigure. Utile : Jackson accepte les floats comme int par défaut — désactivé (accept-float-as-int: false) pour que RG8 rejette vraiment 12.5.

IA : m'a aidé à rechercher le nouveau package de test Spring Boot 4 et proposé la couche signals Angular. Vérifié en exécutant : chaque code HTTP du contrat confronté à sa réponse curl réelle (201/400/403/409/410/429/404), 11 tests relus et passés, ng build et mvn package verts, ordre de vérification D3 testé dans l'ordre, scénario complet présence→dépôt→assignation→relecture→tableau rejoué de bout en bout (moyenne 14.0 lue côté API).

---

## Rattrapage avant l'étape 3 (≈ 16 h 30 – 17 h 30, 25/09)

Fait : re-vérification des tests (les 11 d'origine sont verts — les 6 échecs annoncés ne se reproduisent pas) ; 4 tests d'intégration ajoutés sur `POST /api/presences` (201 nominal, 400 CODE_INCONNU, 409 DEJA_PRESENT, 410 CODE_EXPIRE — B6) ; refactor B3 achevé : `EtudiantService` créé, `listerEnAttente` déplacée dans `RelectureService`, plus aucun contrôleur n'injecte un Repository ; issues GitHub alignées sur le backlog (#12–#14 repassées Must, #27 créée pour le T15 documentation) ; les 4 opérations Must manquantes implémentées et vérifiées par curl : `POST /api/sessions/{id}/cloture` (RG12, 409 SESSION_DEJA_CLOTUREE au second appel), `POST /api/sessions/{id}/presences-manuelles` (source=FORMATEUR, RG11, 409 DEJA_PRESENT en doublon), `PUT /api/exercices/{id}` (RG6, 409 RELECTURE_DEJA_COMMENCEE dès statut assigne/relu, 409 SESSION_CLOTUREE après clôture), `GET /api/exercices/{id}/retour` (Q8 : note + commentaire sans nom du relecteur) ; D2 complété (TENTATIVE_CODE de V3, colonnes alignées au SQL) ; seeds enrichis (V8 : 30 étudiants, session clôturée PAST99 avec 8 exercices, 7 notes rendues et 1 relecture en attente, 2 présences déjà posées sur DEMO24) ; README mis à jour (port alternatif 8081, 15 tests, seeds documentés).

Bloqué : 10 min — premier jet du test présence en échec : clé étrangère PRESENCE_ETUDIANT avec un etudiantId arbitraire, corrigé en créant l'étudiant dans le test. 5 min de doute sur le PUT nominal qui renvoyait 409 : comportement en fait correct, l'exercice avait été assigné dès le dépôt (présence manuelle = candidat relecteur) ; scénario rejoué avec un exercice en_attente_relecteur pour vérifier le 200.

IA : m'a proposé la structure d'`EtudiantService` et le squelette de la migration V8. Vérifié en exécutant : `./mvnw test` 15/15 verts, chaque nouvel endpoint confronté à sa réponse curl réelle (200/201/400/404/409), tableau relu côté API (30 lignes, moyenne null pour l'étudiant sans note, jamais recalculée côté front), relectures/en-attente de l'étudiant 9 = 1 ligne, migrations V1–V8 validées par Flyway au démarrage.

---

## Étape 3 — Conduite du changement (enveloppe)

### A. Bug de concurrence (issue #33, PR #34)

Fait (≈ 45 min) : issue GitHub créée AVANT tout code (#33) ; test d'intégration concurrent écrit (2 threads + CyclicBarrier, vraies transactions, sans `@Transactional`) — confirmé ROUGE avant tout correctif (2 échecs : 500 reçus au lieu de 201/409) ; investigation dans les logs serveur : la piste « agrégat parent @OneToMany + cascade » a été écartée (l'INSERT est déjà autonome), la vraie cause est un TOCTOU — le check RG2 puis INSERT laisse deux transactions passer, la contrainte unique `(session_id, etudiant_id)` rejette le perdant et l'exception remontait en 500 ; découverte en creusant : H2 2.4.240 souffre en plus du bug #4302 (« Check constraint invalid » sous inserts simultanés). Correctif : INSERT atomique encapsulé (`insererDeManiereAtomique`) — la contrainte unique sert de filet de sécurité et le perdant reçoit `409 DEJA_PRESENT` — plus passage H2 2.5.250. VERT : 2/2 puis 17/17.

Bloqué : 15 min — le test rouge de la nuit affichait des erreurs de harnais (collision de code de session entre méthodes : la base H2 nommée survit aux contextes de test ; corrigé par code unique par méthode) avant d'obtenir le rouge produit propre (500).

IA : a proposé le plan de test concurrent et la traduction de l'exception H2 en hypothèse de bug amont, puis vérifié le changelog H2 (issue #4302 corrigée en 2.5.250). Vérifié en exécutant : reproduction réelle par deux curl simultanés sur un serveur lancé (`S1=201 S2=500` + violation d'index dans le log), test rouge montré puis vert, suite complète 17/17, migrations validées sur H2 2.5.250.

### B. Changement de besoin : double relecture (issues #35–#39, PR dédiée)

Fait : analyse d'abord — commit docs isolé (RG16 remplacée « deux relecteurs distincts », RG10/RG15 complétées, EF8/EF9/EF11/EF13 mises à jour, entrée §7 documentant la rupture assumée de Q6, D2 en 0..2, D4 avec `relu_partiel`) ; vérification du contrat AVANT code : `POST /api/relectures/{id}` désigne l'id de l'EXERCICE avec une seule affectation possible — signalé, décision : `relecteurId` OPTIONNEL dans le corps (chemins/verbes/codes imposés inchangés, B2 ; rétrocompatible avec une seule affectation) ; migration V9 (unique `(exercice_id, relecteur_id)`, statut `relu_partiel`, seed enrichi : 6 exercices avec deux notes, un en provisoire, une affectation en attente) — aucune migration antérieure modifiée ; assignation de deux relecteurs distincts (un seul candidat → assignation simple ; aucun → RG14 inchangé) ; retour étudiant = moyenne des relectures rendues + `provisoire` ; tableau = moyennes par exercice puis agrégation (toujours côté API, F3) ; écran relecteur inchangé (il soumet sa propre affectation) ; écran étudiant avec badge « PROVISOIRE ».

Bloqué : 25 min — la migration V9 échouait sur H2 : après le DROP de la contrainte unique, H2 rattache son index à la clé étrangère (`L'index ... appartient à la contrainte FK_RELECTURE_EXERCICE`) ; corrigé en démontant FK + unique + index résiduel puis en remontant le tout (aucune donnée perdue). 10 min : deux vieux tests reposaient sur une coïncidence (id de relecture = id d'exercice, rompue par le seed V9) — assertions resserrées sur des champs réels.

IA : a proposé l'option `relecteurId` dans le corps et la décomposition FK/unique/index de la migration. Vérifié en exécutant : `./mvnw test` 21/21 verts dont 4 nouveaux (une seule rendue → 14.0 + provisoire + `relu_partiel` ; deux rendues → 13.0 = (10+16)/2 + définitive ; RG10 par affectation 409 ; deux affectations sans `relecteurId` → 400 `RELECTEUR_REQUIS`), migrations V1–V9 validées, ng build vert.

**Sacrifice assumé : pour tenir le délai, l'issue #39 (badge « provisoire » dans le tableau du formateur) est sortie du périmètre — la mention reste visible sur l'écran étudiant uniquement, et le cœur du changement (assignation double, calcul de moyenne, migration) est priorisé.**

