# D3 — Séquence : marquer sa présence

> Codes HTTP conformes au contrat imposé : `201` succès, `400` code inconnu, `409` déjà présent, `410` code expiré, `429` blocage après 5 échecs (RG3, ajouté par le client Q4). Format d'erreur `{ code, message }` partout (B4).

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front (Angular)
    participant API as PresenceController
    participant S as PresenceService
    participant R as PresenceRepository

    E->>F: choisit son nom (Q1) et saisit le code
    F->>API: POST /api/presences { code, etudiantId }

    alt 5 échecs précédents → blocage actif (RG3)
        S->>S: vérifier compteur de tentatives (RG3, RG4)
        S-->>API: TropTentativesException
        API-->>F: 429 { "code": "TROP_TENTATIVES", "message": "..." }
    else code inconnu
        S->>R: findByCode(code)
        R-->>S: null
        S-->>API: CodeInconnuException
        API-->>F: 400 { "code": "CODE_INCONNU", "message": "..." }
    else code expiré (RG1)
        S->>R: findByCode(code)
        R-->>S: session
        S->>S: maintenant > expirationAt ?
        S-->>API: CodeExpireException
        API-->>F: 410 { "code": "CODE_EXPIRE", "message": "..." }
    else déjà présent (RG2)
        S->>R: existsBySessionIdAndEtudiantId(sessionId, etudiantId)
        R-->>S: true
        S-->>API: DejaPresentException
        API-->>F: 409 { "code": "DEJA_PRESENT", "message": "..." }
    else cas nominal
        S->>S: incrémenter le compteur de réussite
        S->>R: save(Presence { sessionId, etudiantId, source: ETUDIANT })
        R-->>S: Presence enregistrée
        S-->>API: Presence
        API-->>F: 201 { "id", "sessionId", "etudiantId", "source": "ETUDIANT" }
    end

    F-->>E: message de confirmation ou d'erreur (état de chargement/erreur géré — F3)
```

**Points de contrôle de conformité :**

- Chaque branche d'erreur renvoie exactement le format imposé `{ code, message }`, jamais de stack trace (B4).
- `410 CODE_EXPIRE` vérifie `expirationAt` issu de RG1 — cohérence avec D2 et avec le contrat.
- L'ordre des vérifications est : blocage → code inconnu → code expiré → déjà présent → enregistrement.
