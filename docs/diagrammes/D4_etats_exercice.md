# D4 — États-transitions : cycle de vie d'un exercice (bonus)

> États de `EXERCICE.statut` (D2). Ce diagramme est le bonus du sujet (+3).

```mermaid
stateDiagram-v2
    [*] --> depose : POST /api/exercices (EF5 · RG5)

    depose --> en_attente_relecteur : aucun candidat<br/>aucun autre présent que l'auteur (RG14 · Q11)
    depose --> assigne : assignation aléatoire d'un relecteur présent ≠ auteur (RG13)

    en_attente_relecteur --> assigne : un étudiant devient disponible / le formateur relance l'assignation

    assigne --> relu_partiel : première relecture rendue — note PROVISOIRE (EF9 · RG10 étape 3)
    relu_partiel --> relu : seconde relecture rendue — note finale (moyenne des deux)
    assigne --> relu : les deux relectures rendues (ou un seul relecteur assigné qui rend)
    depose --> depose : PUT /api/exercices/{id} remplacement du lien<br/>si aucune relecture commencée (EF6 · RG6)

    note right of relu
        État final
        RG10 : chaque relecture individuelle est définitive dès l'envoi (Q15)
        Étape 3 : note affichée = moyenne des relectures rendues,
        PROVISOIRE si une seule (changement de besoin étape 3)
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
| `assigne → relu_partiel` | Première relecture rendue (étape 3) | RG10 : définitive pour ce relecteur ; note de l'exercice PROVISOIRE |
| `relu_partiel → relu` | Seconde relecture rendue (étape 3) | Note finale = moyenne des deux relectures rendues |
| `assigne → relu` | Les deux rendues (ou un seul relecteur assigné qui rend) | RG8 note entière 0–20, RG7 pas son propre exercice, RG10 définitive |
| `depose → depose` (auto) | `PUT /api/exercices/{id}` | RG6 : tant que personne n'a relu ; après, `409 RELECTURE_DEJA_COMMENCEE` |
