# Backlog — issues à créer sur GitHub

> `gh` n'étant pas disponible sur ce poste, ce fichier liste les issues prêtes à copier.
> Chaque issue : un titre orienté **résultat**, des critères d'acceptation vérifiables, une priorité **Must / Should / Could**, et le renvoi aux `EFx` / `RGx`.
> Convention de branche : `ticket-<n>-<slug>` (ex. `ticket-1-ouvrir-session`). Les commits ferment l'issue (`Closes #n`) et citent la règle (`RGx`) quand il y en a une.

**Ordre de réalisation :** les tickets Must dans l'ordre, car chaque brique s'appuie sur la précédente (schéma → sessions → présences → exercices → relectures → tableau).

---

### 1. Le formateur peut ouvrir une session et obtenir un code de présence
- **Renvoi :** EF1 · RG1 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/sessions` avec `{ titre, promotionId }` renvoie `201 { id, code, ouvertureAt, expirationAt }`
  - [ ] `expirationAt = ouvertureAt + 15 min` (RG1)
  - [ ] Champ manquant → `400 { code: "CHAMP_MANQUANT", message: ... }`
  - [ ] Migration Flyway V1__*.sql (promotion, etudiant, session) commitée — D2 conforme
- **Branches :** `ticket-1-ouvrir-session`

### 2. L'étudiant peut marquer sa présence avec le code
- **Renvoi :** EF2, EF3 · RG1, RG2 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/presences` code valide → `201 { id, sessionId, etudiantId, source: "ETUDIANT" }`
  - [ ] Code inconnu → `400 CODE_INCONNU` · code expiré → `410 CODE_EXPIRE` (RG1) · déjà présent → `409 DEJA_PRESENT` (RG2)
  - [ ] Format d'erreur `{ code, message }` partout via `@RestControllerAdvice` (B4)
  - [ ] Contrainte d'unicité `(session_id, etudiant_id)` en base (RG2)
- **Branches :** `ticket-2-marquer-presence`

### 3. L'étudiant est bloqué après 5 échecs de code
- **Renvoi :** EF4 · RG3, RG4 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] 5 codes erronés → la 6e tentative, même correcte, renvoie `429 TROP_TENTATIVES` pendant 2 min (RG4)
  - [ ] **Test unitaire** de cette règle métier (B6 — c'est le test unitaire exigé)
  - [ ] Le blocage expire après 2 minutes
- **Branches :** `ticket-3-blocage-cinq-echecs`

### 4. L'étudiant peut déposer le lien de son exercice
- **Renvoi :** EF5 · RG5 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/exercices` lien valide → `201 { id, statut: "depose" }`
  - [ ] Lien invalide → `400 LIEN_INVALIDE` · 2e dépôt même session → `409 EXERCICE_DEJA_DEPOSE` (RG5)
  - [ ] Migration Flyway (exercice) commitée — D2 conforme
- **Branches :** `ticket-4-deposer-exercice`

### 5. Le système assigne au hasard un relecteur à chaque exercice
- **Renvoi :** EF8 · RG13, RG14, RG16, RG7 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] Au dépôt, un relecteur est choisi **au hasard** parmi les présents de la session, **≠ auteur** (RG13, RG7)
  - [ ] Aucun candidat → exercice à l'état `en_attente_relecteur`, visible dans le tableau (RG14, Q11)
  - [ ] Un seul relecteur par exercice (RG16) — migration Flyway (relecture) commitée
- **Branches :** `ticket-5-assignation-relecteur`

### 6. Le relecteur peut rendre une relecture avec note et commentaire
- **Renvoi :** EF9, EF10, EF11 · RG7, RG8, RG10 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/relectures/{id}` note entière 0–20 + commentaire → `200` (RG8)
  - [ ] Note hors bornes / non entière → `400 NOTE_INVALIDE` · son propre exercice → `403 RELECTURE_PROPRE_EXERCICE` (RG7) · déjà rendue → `409 RELECTURE_DEJA_RENDUE` (RG10 — définitive, Q15)
  - [ ] **Test d'intégration** sur cet endpoint (B6 — c'est le test d'intégration exigé)
  - [ ] Aucune API n'expose le nom du relecteur à l'étudiant relu (Q8, EF11)
- **Branches :** `ticket-6-rendre-relecture`

### 7. Le formateur peut clôturer une session
- **Renvoi :** EF7 · RG12 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/sessions/{id}/cloture` → `200`, `clotureAt` renseigné
  - [ ] Après clôture : dépôt d'exercice refusé `409 SESSION_CLOTUREE` (Q3, Q12 — RG12)
  - [ ] Session inconnue → `404 SESSION_INCONNUE` · déjà clôturée → `409 SESSION_DEJA_CLOTUREE`
- **Branches :** `ticket-7-cloturer-session`

### 8. L'étudiant peut remplacer le lien de son exercice
- **Renvoi :** EF6 · RG6 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `PUT /api/exercices/{id}` avant toute relecture → `200`
  - [ ] Après le début d'une relecture → `409 RELECTURE_DEJA_COMMENCEE` (Q13, RG6)
- **Branches :** `ticket-8-remplacer-lien`

### 9. Le formateur peut ajouter une présence manuelle identifiable
- **Renvoi :** EF12 · RG11 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `POST /api/sessions/{id}/presences-manuelles` → `201 { ..., source: "FORMATEUR" }`
  - [ ] « ajouté par le formateur » visible dans le tableau (Q14, RG11)
  - [ ] Déjà présent → `409 DEJA_PRESENT` · après clôture → `409 SESSION_CLOTUREE`
- **Branches :** `ticket-9-presence-manuelle`

### 10. Le formateur voit un tableau de suivi par promotion
- **Renvoi :** EF13, EF14 · RG15 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] `GET /api/tableau?promotionId=` → `200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]` (Q16)
  - [ ] Moyenne **calculée côté API** (F3), `null` si aucune note
  - [ ] Promotion inconnue → `404 PROMOTION_INCONNUE` · réponse < 2 s avec 60 étudiants (ENF2)
- **Branches :** `ticket-10-tableau-formateur`

### 11. Le formateur peut ouvrir une session depuis l'interface et suivre le tableau
- **Renvoi :** EF1, EF7, EF12, EF13 · F2 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] Écran formateur : ouvrir une session (affiche le code), clôturer, présence manuelle, tableau
  - [ ] Appels API uniquement via la couche dédiée `HttpClient` (F3), états de chargement et d'erreur affichés
  - [ ] `ng build` passe (F1)
- **Branches :** `ticket-11-ecran-formateur`

### 12. L'étudiant peut marquer sa présence et déposer son exercice depuis l'interface
- **Renvoi :** EF2, EF5, EF6, EF15 · F2, ENF1 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] Écran étudiant : nom choisi dans une liste (Q1, EF15), code saisi, lien déposé/remplacé
  - [ ] Utilisable à 375 px de large sans scroll horizontal (ENF1)
  - [ ] Erreurs 400/409/410/429 affichées avec le `message` renvoyé par l'API
- **Branches :** `ticket-12-ecran-etudiant`

### 13. Le relecteur peut rendre sa relecture depuis l'interface
- **Renvoi :** EF9 · F2 — priorité **Must**
- **Critères d'acceptation :**
  - [ ] Écran relecteur : liste des exercices à relire, saisie note (0–20 entier) + commentaire
  - [ ] `403` / `409` affichés clairement ; confirmation que la note est définitive (RG10)
- **Branches :** `ticket-13-ecran-relecteur`

### 14. Le correcteur peut démarrer l'application avec des données de démonstration
- **Renvoi :** ENF3, ENF5 · contrainte « Démarrage » du sujet — priorité **Should**
- **Critères d'acceptation :**
  - [ ] `docker compose up` **ou** 3 commandes max dans le README, testées depuis un clone vierge
  - [ ] Données de démo chargées au démarrage : 1 promotion, ~30 étudiants, 2 sessions, exercices et relectures
- **Branches :** `ticket-14-demarrage-demo`

### 15. L'application est documentée et livrable (README, CHANGELOG)
- **Renvoi :** étapes 4 du sujet · F1 — priorité **Should**
- **Critères d'acceptation :**
  - [ ] README : choix d'Angular justifié en une ligne, installation testée depuis un clone vierge
  - [ ] `CHANGELOG.md` cohérent avec l'historique
  - [ ] Backlog restant trié (Could fermées ou reportées)
- **Branches :** `ticket-15-documentation`

---

**Non retenues (Could, volontairement hors périmètre §3) :** notifications e-mail, multi-relecteurs, authentification, historique des liens remplacés. Leur exclusion est documentée dans le cahier des charges — ne pas les ouvrir en issue.
