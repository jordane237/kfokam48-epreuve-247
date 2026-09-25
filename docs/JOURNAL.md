# Journal de bord — kfokam48-epreuve-247

> Une entrée par étape. Trois lignes : ce que je viens de faire, ce qui m'a bloqué et combien de temps, ce que j'ai demandé à l'IA et comment j'ai vérifié sa réponse.

---

## Étape 1 — Analyse et conception

Fait : cahier des charges complet (15 EF avec critères vérifiables, 16 RG référencées aux Qx), 4 diagrammes Mermaid (D1 cas d'utilisation, D2 classes, D3 séquence présence, D4 états de l'exercice — bonus), 15 issues rédigées dans `docs/backlog-issues.md` (gh indisponible), contrat `api/contrat.yaml` complété (5 opérations imposées + 3 ajoutées), jalon `[JALON] analyse` posé avant tout commit de code.

Bloqué : 10 min — `gh` absent du poste, issues non créées directement sur GitHub, je les créerai par copier-coller depuis `docs/backlog-issues.md`. 15 min sur la contradiction Q10/Q15 avant de comprendre que le `409 RELECTURE_DEJA_RENDUE` du contrat interdisait déjà toute correction : j'ai tranché Q15 (RG10), RG9 (Q10) marquée abandonnée.

IA : je lui ai fourni CLIENT.md et mes 6 arbitrages imposés, et demandé cahier des charges, diagrammes, contrat et backlog. Vérification : j'ai relu chaque code HTTP du contrat contre l'Annexe B du sujet (5 opérations inchangées, format d'erreur `{ code, message }` partout), contrôlé que D2 correspond aux futures tables Flyway (unicités RG2/RG5, nullable `cloture_at` RG12) et que D3 renvoie exactement 400/409/410/429 dans l'ordre blocage → inconnu → expiré → déjà présent ; relu les 15 issues pour n'en garder aucune qui soit une tâche technique plutôt qu'un résultat utilisateur.

---

## Étape 2 — Première version (v0.1)

Fait : les 11 tickets Must implémentés, chacun sur sa branche, une PR par ticket fermant l'issue (`Closes #n`) : T1 squelette Spring Boot 4 + Angular 20, T2 POST /api/sessions, T3 présences avec blocage RG4, T4 dépôt d'exercice, T5 assignation aléatoire du relecteur, T6 relecture définitive RG10, T7 tableau du formateur, T8-T10 les trois écrans, T11 seed + CORS + README. 11 tests verts (unitaires RG1/RG2/RG4/RG13/RG14 + intégration MockMvc), chaque endpoint vérifié par curl contre le contrat.

Bloqué : 20 min — port 8080 occupé par un autre processus, tests basculés sur 8081 via SERVER_PORT. 15 min sur un test RG2 qui ne passait pas : id d'entité non persistée = null, `anyLong()` de Mockito ne matche pas, corrigé avec `any()`. 10 min : Spring Boot 4 a déplacé @AutoConfigureMockMvc dans org.springframework.boot.webmvc.test.autoconfigure. Utile : Jackson accepte les floats comme int par défaut — désactivé (accept-float-as-int: false) pour que RG8 rejette vraiment 12.5.

IA : m'a aidé à rechercher le nouveau package de test Spring Boot 4 et proposé la couche signals Angular. Vérifié en exécutant : chaque code HTTP du contrat confronté à sa réponse curl réelle (201/400/403/409/410/429/404), 11 tests relus et passés, ng build et mvn package verts, ordre de vérification D3 testé dans l'ordre, scénario complet présence→dépôt→assignation→relecture→tableau rejoué de bout en bout (moyenne 14.0 lue côté API).

