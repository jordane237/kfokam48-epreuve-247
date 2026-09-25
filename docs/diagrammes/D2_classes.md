# D2 — Modèle de données (classes)

> Ce diagramme doit rester cohérent avec les migrations Flyway de l'étape 2. Toute évolution du schéma passe par une migration **et** une mise à jour de ce diagramme (sujet, étape 3).

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "contient"
    PROMOTION ||--o{ SESSION : "concerne"
    SESSION ||--o{ PRESENCE : "recense"
    SESSION ||--o{ EXERCICE : "receoit"
    ETUDIANT ||--o{ PRESENCE : "marque"
    ETUDIANT ||--o{ EXERCICE : "depose"
    ETUDIANT ||--o{ RELECTURE : "effectue_en_tant_que_relecteur"
    EXERCICE ||--o| RELECTURE : "fait_l_objet_de"

    PROMOTION {
        bigint id PK
        varchar nom
    }

    ETUDIANT {
        bigint id PK
        bigint promotion_id FK
        varchar nom
    }

    SESSION {
        bigint id PK
        bigint promotion_id FK
        varchar titre
        varchar code UK
        timestamp ouverture_at
        timestamp expiration_at "RG1 : ouverture + 15 min"
        timestamp cloture_at "nullable — RG12, clôture manuelle"
    }

    PRESENCE {
        bigint id PK
        bigint session_id FK
        bigint etudiant_id FK "UK (session_id, etudiant_id) — RG2"
        varchar source "ETUDIANT | FORMATEUR — RG11"
        timestamp marque_a
    }

    EXERCICE {
        bigint id PK
        bigint session_id FK "UK (session_id, etudiant_id) — RG5"
        bigint etudiant_id FK
        varchar lien
        varchar statut "depose | en_attente_relecteur | assigne | relu — D4"
        timestamp depose_a
        timestamp maj_a
    }

    RELECTURE {
        bigint id PK
        bigint exercice_id FK "UK — un seul relecteur, RG16"
        bigint relecteur_id FK "≠ auteur — RG7, parmi les présents RG13"
        int note "entier 0–20 — RG8"
        text commentaire
        varchar statut "assignee | rendue — RG10 : rendue = définitive"
        timestamp assignee_a
        timestamp rendue_a
    }
```

**Notes de conception :**

- Unicité `(session_id, etudiant_id)` sur `PRESENCE` (RG2) et sur `EXERCICE` (RG5) : garantie en base par contrainte d'unicité, pas seulement dans le service.
- `SESSION.cloture_at` est nullable : `NULL` = session ouverte (le dépôt reste possible, Q12), renseigné = clôturée (RG12).
- `RELECTURE.relecteur_id` porte le choix aléatoire (RG13) : le relecteur est un étudiant, pas un acteur distinct (§2).
- La note n'existe que sur `RELECTURE` ; la moyenne du tableau (EF13) est calculée côté API (F3).
