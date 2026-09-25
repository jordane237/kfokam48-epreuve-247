# Journal de bord — kfokam48-epreuve-247

> Une entrée par étape. Trois lignes : ce que je viens de faire, ce qui m'a bloqué et combien de temps, ce que j'ai demandé à l'IA et comment j'ai vérifié sa réponse.

---

## Étape 1 — Analyse et conception

Fait : cahier des charges complet (15 EF avec critères vérifiables, 16 RG référencées aux Qx), 4 diagrammes Mermaid (D1 cas d'utilisation, D2 classes, D3 séquence présence, D4 états de l'exercice — bonus), 15 issues rédigées dans `docs/backlog-issues.md` (gh indisponible), contrat `api/contrat.yaml` complété (5 opérations imposées + 3 ajoutées), jalon `[JALON] analyse` posé avant tout commit de code.

Bloqué : 10 min — `gh` absent du poste, issues non créées directement sur GitHub, je les créerai par copier-coller depuis `docs/backlog-issues.md`. 15 min sur la contradiction Q10/Q15 avant de comprendre que le `409 RELECTURE_DEJA_RENDUE` du contrat interdisait déjà toute correction : j'ai tranché Q15 (RG10), RG9 (Q10) marquée abandonnée.

IA : je lui ai fourni CLIENT.md et mes 6 arbitrages imposés, et demandé cahier des charges, diagrammes, contrat et backlog. Vérification : j'ai relu chaque code HTTP du contrat contre l'Annexe B du sujet (5 opérations inchangées, format d'erreur `{ code, message }` partout), contrôlé que D2 correspond aux futures tables Flyway (unicités RG2/RG5, nullable `cloture_at` RG12) et que D3 renvoie exactement 400/409/410/429 dans l'ordre blocage → inconnu → expiré → déjà présent ; relu les 15 issues pour n'en garder aucune qui soit une tâche technique plutôt qu'un résultat utilisateur.

---
