# D4 — États-transitions : cycle de vie d'un exercice (bonus)

> États de `EXERCICE.statut` (D2). Ce diagramme est le bonus du sujet (+3).

```mermaid
stateDiagram-v2
    [*] --> depose : POST /api/exercices (EF5 · RG5)

    depose --> en_attente_relecteur : aucun candidat<br/>aucun autre présent que l'auteur (RG14 · Q11)
    depose --> assigne : assignation aléatoire d'un relecteur présent ≠ auteur (RG13)

    en_attente_relecteur --> assigne : un étudiant devient disponible / le formateur relance l'assignation

    assigne --> relu : POST /api/relectures/{id} note + commentaire (EF9 · RG8 RG10)
    depose --> depose : PUT /api/exercices/{id} remplacement du lien<br/>si aucune relecture commencée (EF6 · RG6)

    note right of relu
        État final
        RG10 : note définitive
        dès l'envoi (Q15)
    end note

    note right of assigne
        PUT /api/exercices/{id} → 409 RELECTURE_DEJA_COMMENCEE (RG6)
    end note
```

**Transitions et leurs règles :**

| Transition | Déclencheur | Règles |
|---|---|---|
| `[*] → depose` | `POST /api/exercices` | RG5 : un seul exercice par étudiant et par session (`409 EXERCICE_DEJA_DEPOSE`) |
| `depose → en_attente_relecteur` | Assignation impossible | RG14 : aucun étudiant présent autre que l'auteur ; visible dans le tableau (Q11) |
| `depose → assigne` | Assignation automatique | RG13 : au hasard parmi les présents, hors auteur (RG7) |
| `en_attente_relecteur → assigne` | Un candidat devient disponible | RG13, RG14 |
| `assigne → relu` | `POST /api/relectures/{id}` | RG8 note entière 0–20, RG7 pas son propre exercice, RG10 définitive |
| `depose → depose` (auto) | `PUT /api/exercices/{id}` | RG6 : tant que personne n'a relu ; après, `409 RELECTURE_DEJA_COMMENCEE` |
