# Cahier des charges — KFOKAM48 Présence & Relecture par les pairs

**Auteur :** jordane237 · KF48-247
**Version :** 1 · **Date :** 2026-09-25
**Frontend choisi :** Angular, parce que le projet impose une séparation stricte des couches (composants / services / couche d'appel API dédiée, contrainte F3) et qu'Angular structure cela nativement : injection de dépendances, services `HttpClient` centralisés, typage TypeScript de bout en bout, et un CLI qui garantit un build reproductible.

> Ce document décrit le besoin AVANT l'étape 3 (ouverture de l'enveloppe). Toute décision cite sa source (`Qx` de CLIENT.md, exigence `EFx`, règle `RGx`).

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 anime des sessions de cours avec des promotions d'environ 30 étudiants. Aujourd'hui, la prise de présence et la remise d'exercices se font de façon informelle (appels verbaux, liens envoyés par messagerie), ce qui rend le suivi difficile et la correction subjective.

L'application répond à un besoin concret en cinq temps : le formateur ouvre une session et obtient un code de présence ; chaque étudiant marque sa présence avec ce code ; il dépose ensuite le lien de son exercice ; le système assigne au hasard un autre étudiant présent pour relire cet exercice (note sur 20 + commentaire) ; le formateur suit le tout dans un tableau de bord (présences, exercices déposés, moyenne des notes, relectures en attente).

L'objectif n'est pas de remplacer une plateforme de cours : l'application couvre **uniquement** le cycle présence → dépôt → relecture → suivi, par session.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session (obtient le code), clôturer la session (RG12), ajouter une présence manuelle marquée « ajouté par le formateur » (Q14, RG11), consulter le tableau de suivi (Q16) | Marquer sa propre présence, déposer un exercice, relire, modifier une note |
| **Étudiant** | Marquer sa présence avec le code (RG1–RG4), déposer le lien de son exercice (Q12, RG5), remplacer son lien tant que personne n'a relu (Q13, RG6), consulter sa note et le commentaire reçus (Q8) | Se relire lui-même (Q5, RG7), voir le nom de son relecteur (Q8), déposer après la clôture (RG12), modifier son lien après le début d'une relecture (RG6) |
| **Relecteur** | Rendre une relecture : note entière 0–20 + commentaire (Q9, RG8), tant que la session n'est pas clôturée (RG9) | Relire son propre exercice (RG7), corriger sa note après envoi (Q15, RG10), voir les relectures des autres |

**Choix de modélisation :** le relecteur **n'est pas un acteur distinct**. C'est un étudiant dans un certain état (assigné à une relecture), identifié par `etudiantId` dans l'entité `Relecture`. Conséquence sur le modèle de données : pas d'entité ni de rôle « Relecteur » — l'entité `Relecture` porte la relation vers l'étudiant assigné (D2). L'écran de relecture est un écran de l'étudiant qui a une relecture en attente.

L'identification est sans mot de passe : chaque participant choisit son nom dans une liste (Q1). Il n'y a donc pas d'authentification, seulement une sélection d'identité.

## 3. Périmètre

**Inclus dans cette version :**

- Ouverture de session avec code de présence expirant 15 min après l'ouverture (RG1)
- Marquage de présence par code, avec blocage 2 min après 5 échecs (RG2, RG3, RG4)
- Présence manuelle ajoutée par le formateur, identifiable (source `FORMATEUR`, RG11)
- Dépôt du lien d'exercice par session, jusqu'à la clôture (RG5, RG12)
- Remplacement du lien tant qu'aucune relecture n'a commencé (RG6)
- Assignation automatique au hasard d'un relecteur parmi les présents, hors auteur (RG13, RG14)
- Soumission d'une relecture (note entière 0–20 + commentaire), définitive (RG8, RG10)
- Clôture de session par le formateur (RG12)
- Tableau de bord formateur : présences, exercices déposés, moyenne des notes reçues, relectures en attente (RG15)
- Consultation par l'étudiant de sa note et du commentaire (sans nom du relecteur)

**Explicitement exclu :**

- Toute authentification par mot de passe, inscription, gestion de comptes (Q1)
- Édition ou hébergement des exercices : seul un **lien** est déposé
- Notification par e-mail ou SMS (y compris pour signaler une relecture en attente — Q11 est couvert par l'affichage dans le tableau)
- Plusieurs relecteurs par exercice (Q6 : un seul)
- Modification d'une note après envoi (Q15, tranché — voir §7)
- Réunions d'évaluation, rattrapages, sessions passées hors promotions gérées
- Applications mobiles natives (le web responsive suffit, ENF1)

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session et obtient un code de présence | Quand je poste `POST /api/sessions` avec un titre et une promotion, je reçois `201` avec `{ id, code, ouvertureAt, expirationAt }` et `expirationAt = ouvertureAt + 15 min` (RG1) | Must |
| EF2 | L'étudiant marque sa présence avec le code | Quand je poste `POST /api/presences` avec un code valide et non expiré, je reçois `201 { id, sessionId, etudiantId, source }` et ma présence apparaît dans le tableau du formateur | Must |
| EF3 | L'étudiant voit une erreur claire quand le code est inconnu, expiré ou déjà utilisé | Quand je poste un code inconnu → `400 { code: "CODE_INCONNU" }` ; un code expiré → `410 { code: "CODE_EXPIRE" }` (RG1) ; une 2e présence → `409 { code: "DEJA_PRESENT" }` (RG2) ; corps au format `{ code, message }` | Must |
| EF4 | Le système bloque temporairement un étudiant après 5 échecs de code | Quand je saisis 5 codes erronés, ma 6e tentative — même correcte — renvoie `429 { code: "TROP_TENTATIVES" }` pendant 2 minutes (RG3) | Must |
| EF5 | L'étudiant dépose le lien de son exercice pour une session | Quand je poste `POST /api/exercices` avec un lien valide, je reçois `201 { id, statut: "depose" }` ; un 2e dépôt pour la même session → `409 { code: "EXERCICE_DEJA_DEPOSE" }` (RG5) ; un lien invalide → `400 { code: "LIEN_INVALIDE" }` | Must |
| EF6 | L'étudiant remplace le lien de son exercice tant que personne n'a relu | Quand je fais `PUT /api/exercices/{id}` avec un nouveau lien avant toute relecture → `200` ; si une relecture a commencé → `409 { code: "RELECTURE_DEJA_COMMENCEE" }` (RG6) | Must |
| EF7 | Le formateur clôture la session | Quand je poste `POST /api/sessions/{id}/cloture` → `200` ; ensuite tout dépôt de présence (manuelle) ou d'exercice est refusé `409 { code: "SESSION_CLOTUREE" }` (RG12) | Must |
| EF8 | Le système assigne automatiquement **deux relecteurs distincts** à chaque exercice déposé | Quand un exercice est déposé, le système choisit au hasard **deux** étudiants présents à la session, différents de l'auteur et l'un de l'autre (RG13, RG16) ; l'exercice passe à l'état `assigne` ; s'il n'y a qu'un seul candidat, il est assigné seul (la note sera provisoire jusqu'à la deuxième relecture) ; s'il n'y en a aucun, l'exercice reste `en_attente_relecteur` (Q11, RG14) | Must |
| EF9 | Le relecteur soumet une note et un commentaire | Quand je poste `POST /api/relectures/{id}` avec une note entière 0–20 et un commentaire → `200` (RG8) ; note hors bornes ou non entière → `400 { code: "NOTE_INVALIDE" }` ; ma relecture déjà rendue → `409 { code: "RELECTURE_DEJA_RENDUE" }` (RG10). **Étape 3 :** chaque relecteur soumet via SA propre affectation — les deux relecteurs d'un exercice rendent indépendamment | Must |
| EF10 | Le relecteur ne peut pas relire son propre exercice | Quand je tente de rendre une relecture sur mon propre exercice → `403 { code: "RELECTURE_PROPRE_EXERCICE" }` (RG7) | Must |
| EF11 | L'étudiant relu voit sa note et les commentaires, sans le nom des relecteurs | **Étape 3 :** la note affichée est la **moyenne des relectures rendues** de mon exercice ; si une seule est rendue, elle porte la mention **PROVISOIRE** ; l'identité des relecteurs n'est exposée par aucune API de consultation (Q8) | Must |
| EF12 | Le formateur ajoute une présence manuelle identifiable | Quand je poste `POST /api/sessions/{id}/presences-manuelles` → `201 { ..., source: "FORMATEUR" }` visible comme « ajouté par le formateur » dans le tableau (Q14, RG11) ; après clôture → `409 SESSION_CLOTUREE` | Must |
| EF13 | Le formateur consulte un tableau de suivi par promotion | Quand j'appelle `GET /api/tableau?promotionId=`, je reçois `200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]` ; la moyenne agrège les moyennes **par exercice** (moyenne des relectures rendues, provisoire si < 2), calculée côté API, jamais recalculée côté front (F3) ; promotion inconnue → `404 { code: "PROMOTION_INCONNUE" }` (Q16, RG15) | Must |
| EF14 | Le formateur et les étudiants consultent les relectures en attente | Le tableau affiche `relecturesEnAttente` par étudiant (Q16) et l'exercice sans relecteur disponible reste visible « en attente » (Q11) | Should |
| EF15 | L'écran de marquage de présence propose la liste des étudiants | Quand j'ouvre l'écran, je choisis mon nom dans une liste déroulante puis je saisis le code (Q1) | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'interface (marquage de présence surtout) est utilisable sur un téléphone en 375 px de large | Ouverture de l'écran de présence dans une vue mobile du navigateur : saisie du nom et du code sans scroll horizontal ni élément coupé |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Chronométrage de `GET /api/tableau?promotionId=` avec 60 étudiants et 10 sessions en base de démonstration |
| ENF3 | Volumétrie cible : ~30 étudiants par session, quelques sessions par jour ; pas de forte concurrence | Jeu de données de démonstration à cette échelle ; aucune optimisation prématurée documentée |
| ENF4 | Toute erreur API renvoie le format `{ code, message }` sans stack trace | `@RestControllerAdvice` centralisé ; test d'intégration sur au moins une erreur (contrainte B4) |
| ENF5 | Le schéma de base est versionné et reproductible depuis un clone vierge | Migrations Flyway commitées ; `docker compose up` ou 3 commandes du README reconstruisent tout (B5) |
| ENF6 | Deux tests prouvent des règles réelles : RG3 (blocage 5 échecs) en unitaire, EF3 (erreurs de présence) en intégration sur un endpoint | `mvn test` passe sur un poste vierge sans base locale (B6) |
| ENF7 | Aucune règle métier dupliquée côté front : la moyenne vient de l'API | Lecture du code front : aucune division/somme sur les notes dans les composants (F3) |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session ; après, il ne marche plus | Q2 |
| RG2 | Un étudiant ne peut marquer qu'une seule présence par session (unicité) | Q3, implicite |
| RG3 | Après 5 erreurs consécutives de code, l'étudiant est bloqué 2 minutes | Q4 |
| RG4 | Le blocage de RG3 s'applique même si le code saisi est ensuite correct | Q4 (« bloquez-le deux minutes ») |
| RG5 | Un étudiant ne peut déposer qu'un seul exercice par session | Q12 (« son exercice ») + contrat `409` sur `POST /api/exercices` |
| RG6 | Le lien d'un exercice ne peut être remplacé que tant que personne n'a commencé à le relire | Q13 |
| RG7 | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| RG8 | Une note est un entier compris entre 0 et 20 | Q9 |
| RG9 | Un relecteur peut modifier sa note tant que le formateur n'a pas clôturé la session | Q10 — **abandonnée, voir RG10 et §7** |
| RG10 | Une relecture est définitive dès son envoi : plus aucune correction possible. **Étape 3 :** chaque relecture *individuelle* reste définitive — inchangé — mais la note **affichée à l'étudiant** est la moyenne des relectures *rendues* de son exercice ; si une seule est rendue, elle est affichée comme **PROVISOIRE** en attendant la seconde | Q15, complétée par le changement de besoin étape 3 (voir §7) |
| RG11 | Une présence ajoutée à la main par le formateur porte la mention « ajouté par le formateur » (source `FORMATEUR`) | Q14 |
| RG12 | Le dépôt d'exercices (et la présence manuelle) est possible jusqu'à la clôture de la session, pas après ; la présence par code, elle, reste impossible après la fin de la session | Q3, Q12 |
| RG13 | Le relecteur est choisi par le système, au hasard, parmi les étudiants présents à cette session | Q7 |
| RG14 | Le relecteur est différent de l'auteur ; s'il n'existe aucun candidat (aucun autre présent), l'exercice reste `en_attente_relecteur` et le formateur le voit dans son tableau | Q5, Q7, Q11 |
| RG15 | Le tableau du formateur montre, par étudiant : sa présence à chaque session, le nombre d'exercices déposés, la moyenne des notes reçues, et les relectures qu'il doit encore faire. **Étape 3 :** la moyenne par exercice est la moyenne des relectures *rendues* de cet exercice (1 ou 2 notes), avec un indicateur provisoire si moins de 2 rendues ; la moyenne globale de l'étudiant agrège ces moyennes par exercice | Q16, complétée par le changement de besoin étape 3 |
| RG16 | Chaque exercice est relu par **deux relecteurs distincts**, tirés au hasard parmi les étudiants présents à la session, hors auteur ; un seul si un seul candidat est disponible ; aucun → `en_attente_relecteur` (RG14) | Q6, **remplacée par le changement de besoin de l'étape 3** (double relecture — voir §7) |

## 7. Zones d'ombre, hypothèses et contradictions tranchées

**Points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| La « clôture de session » n'est définie nulle part (elle apparaît en Q10, Q12, Q15) alors que l'expiration du code est définie (Q2) | Trou identifié — aucune question ne la définit | La clôture est une action **manuelle** du formateur, opération ajoutée `POST /api/sessions/{id}/cloture`. Elle est distincte de RG1 : un code peut être expiré (plus de présence par code) alors que la session reste ouverte au dépôt (Q12) | Deux états de temps distincts dans le modèle : `expirationAt` (code) et `clotureAt` (session). Une opération supplémentaire dans le contrat, hors des 5 imposées |
| Le dépôt d'exercice exige-t-il d'avoir marqué sa présence ? | Hypothèse — non précisé par le client | **Non** : le dépôt ne vérifie pas la présence. Documenté comme hypothèse, pas implémenté comme contrainte bloquante | Aucun contrôle présence→dépôt dans le service ; si le client tranche plus tard, c'est une règle à ajouter (un point d'extension dans le service) |
| Qui relit si aucun autre étudiant n'est présent ? | Hypothèse ( prolonge Q7 + Q11) | L'exercice reste à l'état `en_attente_relecteur`, visible comme tel dans le tableau du formateur (Q11) | État `en_attente_relecteur` dans le cycle de vie de l'exercice (D4) |
| **Changement de besoin étape 3 (25/09, venu de l'enveloppe) : double relecture** | Demande explicite du client pendant l'étape 3 : « chaque exercice est relu par DEUX pairs distincts ; note retenue = moyenne des deux ; si une seule relecture est rendue, sa note est affichée comme PROVISOIRE » | **Rupture assumée de RG16 (issue de Q6, « un seul relecteur »)** : l'assignation tire désormais deux relecteurs distincts parmi les présents hors auteur ; chaque relecture individuelle reste définitive (RG10 inchangé) ; la note affichée = moyenne des relectures rendues, provisoire si une seule. NB : la demande parlait de « RG3 » ; dans ce CDC la règle « un seul relecteur » est **RG16** (Q6) — RG3 restant le blocage anti brute-force. Contrat B2 : les 5 opérations imposées sont inchangées — la double soumission passe par les affectations individuelles (voir journal) | Migration V9 : `unique(exercice_id)` → `unique(exercice_id, relecteur_id)` ; D2 cardinalité 0..2 ; issue Should sacrifiée pour tenir le délai (voir journal) |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| **Q10 vs Q15** : Q10 dit que le relecteur peut corriger sa note tant que la session n'est pas clôturée ; Q15 dit que la note est définitive dès l'envoi | **Q15** — note définitive dès l'envoi | Le contrat d'API impose `409 RELECTURE_DEJA_RENDUE` sur `POST /api/relectures/{id}` et ne prévoit **aucune opération de correction**. Implémenter Q10 exigerait d'inventer un endpoint hors contrat et contredirait le `409`. Q10 est donc abandonnée (RG9 marquée abandonnée au profit de RG10) |

**Q13 et Q14 face au contrat imposé :** les 5 opérations imposées ne permettent ni de remplacer un lien (Q13) ni d'ajouter une présence manuelle (Q14, car `POST /api/presences` exige un code). Le contrat autorise explicitement des opérations supplémentaires : j'ajoute `PUT /api/exercices/{id}` (EF6, RG6) et `POST /api/sessions/{id}/presences-manuelles` avec `source=FORMATEUR` (EF12, RG11), plus `POST /api/sessions/{id}/cloture` (EF7, RG12).

## 8. Contraintes techniques

**Imposées par le sujet :**

- **B1** — Java 17+ (Java 21 disponible), Maven, wrapper `mvnw` commité
- **B2** — `api/contrat.yaml` respecté à la lettre : chemins, verbes, codes de statut, format d'erreur `{ code, message }`
- **B3** — Séparation contrôleur / service / repository ; entités JPA jamais exposées en JSON, passage par des DTO
- **B4** — Validation des entrées + gestion centralisée des erreurs `@RestControllerAdvice`, aucune stack trace au client
- **B5** — Schéma versionné par Flyway, migrations commitées ; `ddl-auto=update` interdit hors tests
- **B6** — Deux tests : un unitaire sur une règle métier réelle (RG3), un d'intégration sur un endpoint (EF3) ; tournent sans base locale
- **F1** — Angular déclaré et justifié (voir en-tête), build qui passe
- **F2** — Trois écrans : formateur (ouvrir session, clôturer, présence manuelle, tableau), étudiant (présence, dépôt/remplacement d'exercice, voir sa note), relecteur (rendre la relecture)
- **F3** — Appels API dans une couche dédiée (services Angular `HttpClient`), états de chargement et d'erreur gérés, aucune règle métier dupliquée (la moyenne vient de l'API)

**Choisies par moi :**

- Base de données : H2 en mémoire pour les tests/démonstration, PostgreSQL pour l'exécution réelle — les migrations Flyway restent les mêmes
- Gestion du hasard de l'assignation (RG13) dans le service, testable par injection d'un fournisseur d'aléatoire
- `.gitignore` Java + Node posé avant tout commit de code (fait au départ du dépôt)
- Données de démonstration chargées au démarrage (promotions, étudiants, sessions) pour un correcteur qui ouvre une application vivante

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md` — ce document (10 sections)
- `docs/diagrammes/` — D1 cas d'utilisation, D2 classes/modèle de données, D3 séquence « marquer sa présence », D4 états de l'exercice (bonus) — en Mermaid versionné
- `docs/JOURNAL.md` — journal de bord, une entrée par étape
- `docs/backlog-issues.md` — backlog en issues (titres, critères d'acceptation, priorité, renvoi EFx/RGx)
- `api/contrat.yaml` — contrat d'API : 5 opérations imposées + 3 ajoutées, format d'erreur uniforme
- `backend/` — Spring Boot (à l'étape 2 uniquement)
- `frontend/` — Angular (à l'étape 2 uniquement)
- `README.md`, `CHANGELOG.md` (à l'étape 4)
- Historique Git : commits atomiques, une branche par ticket, PR liées aux issues, jalons `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`

## 10. Démarche prévue

1. **Étape 1 — Analyse (maintenant)** : ce cahier des charges, les 4 diagrammes, le backlog en issues, le contrat complété, jalon `[JALON] analyse` **avant tout commit de code**.
2. **Étape 2 — v0.1** : stories **Must** uniquement, une branche par ticket, une PR par branche, issues fermées par les commits, migrations Flyway dès la première entité. Jalon `[JALON] v0.1`.
3. **Étape 3 — Enveloppe** : ouverture du script, reproduction du bug, issue avant de coder, migration versionnée, contrat mis à jour, **mise à jour de ce cahier des charges et des diagrammes** dans un commit dédié.
4. **Étape 4 — v1.0** : correctif + évolution, jalon `[JALON] v1.0`, `CHANGELOG.md` cohérent, README testé depuis un clone vierge, backlog restant trié.
5. **Étape 5 — Épreuve Git** : dépôt séparé `kfokam48-gitlab-247`, jamais mélangé au projet.
6. **Étape 6 — Soumission** : `SOUMISSION.md` téléversé bien avant 18h00, hash des derniers commits des deux dépôts.

**En cas de retard :** je sacrifie d'abord les Should/Could du produit, jamais l'analyse ni l'hygiène Git (38 + 32 points contre 15).

**Definition of Done — un ticket est terminé quand :**

- Les critères d'acceptation de l'issue sont vérifiables et vérifiés
- Le code est sur une branche dédiée, relue via une PR qui référence l'issue (`Closes #n`)
- Le commit cite la règle concernée (`RGx`) quand il en implémente une
- Les migrations Flyway sont commitées si le schéma change
- Le contrat d'API est respecté à la lettre (chemins, verbes, codes, format d'erreur)
- `main` reste sain : build et tests passent après merge
- Aucun fichier généré ni secret commité (`.gitignore` respecté)

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 2026-09-25 | Version initiale — étape 1 (analyse), frontend Angular |
