-- V7 : session de démonstration ouverte au démarrage (l'application n'est jamais vide).
-- Code fixe sans caractères ambigus : le correcteur peut tester immédiatement
-- le marquage de présence. Expiration 30 min après le démarrage
-- (H2 en mémoire réinitialisée à chaque lancement — marge confortable).

INSERT INTO session (promotion_id, titre, code, ouverture_at, expiration_at, cloture_at)
VALUES (1, 'Session de démonstration', 'DEMO24',
        CURRENT_TIMESTAMP,
        TIMESTAMPADD(MINUTE, 30, CURRENT_TIMESTAMP),
        NULL);
