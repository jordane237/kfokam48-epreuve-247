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
    ETUDIANT ||--o| TENTATIVE_CODE : "voit_ses_echecs_suivis"
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
        timestamp marque_at
    }

    EXERCICE {
        bigint id PK
        bigint session_id FK "UK (session_id, etudiant_id) — RG5"
        bigint etudiant_id FK
        varchar lien
        varchar statut "depose | en_attente_relecteur | assigne | relu — D4"
        timestamp depose_at
        timestamp maj_at
    }

    RELECTURE {
        bigint id PK
        bigint exercice_id FK "UK — un seul relecteur, RG16"
        bigint relecteur_id FK "≠ auteur — RG7, parmi les présents RG13"
        int note "entier 0–20 — RG8"
        varchar commentaire
        varchar statut "assignee | rendue — RG10 : rendue = définitive"
        timestamp assignee_at
        timestamp rendue_at
    }

    TENTATIVE_CODE {
        bigint id PK
        bigint etudiant_id FK "UK — un seul suivi par étudiant (Q4)"
        int echecs_consicutifs "défaut 0 — RG3"
        timestamp bloque_jusqua "nullable — RG4 : blocage 2 min après 5 échecs"
    }
```

**Notes de conception :**

- Unicité `(session_id, etudiant_id)` sur `PRESENCE` (RG2) et sur `EXERCICE` (RG5) : garantie en base par contrainte d'unicité, pas seulement dans le service.
- `SESSION.cloture_at` est nullable : `NULL` = session ouverte (le dépôt reste possible, Q12), renseigné = clôturée (RG12).
- `RELECTURE.relecteur_id` porte le choix aléatoire (RG13) : le relecteur est un étudiant, pas un acteur distinct (§2).
- `TENTATIVE_CODE` (migration V3) porte le compteur anti-dévination RG3/RG4 : une seule ligne par étudiant (`UNIQUE (etudiant_id)`), car avec un code inconnu la session visée est par définition inconnue — le blocage porte sur l'étudiant, ce qui est plus strict et conforme à l'intention de Q4.
- La note n'existe que sur `RELECTURE` ; la moyenne du tableau (EF13) est calculée côté API (F3).
- Les noms de colonnes reflètent exactement les migrations Flyway V1–V7 (`marque_at`, `depose_at`, `maj_at`, `assignee_at`, `rendue_at`, `echecs_consicutifs`, `bloque_jusqua`).
