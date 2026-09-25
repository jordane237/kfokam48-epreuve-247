-- V8 : seeds complets pour la démo (ENF3, ticket T11/T15) — le tableau du formateur
-- n'est jamais quasi vide à l'ouverture : ~30 étudiants, 2 sessions (une passée et
-- clôturée, riche en exercices et relectures ; une ouverte pour tester en direct).
-- Hypothèse : exécutée sur une base neuve où V1–V7 ont inséré la promotion id=1,
-- les étudiants id=1..6 et la session id=1 (DEMO24) — les identifiants ci-dessous
-- en découlent (H2 en mémoire, réinitialisée à chaque démarrage).

-- 1) 24 étudiants supplémentaires (ids 7 à 30) — liste de choix de la connexion (Q1)
INSERT INTO etudiant (promotion_id, nom) VALUES
    (1, 'Gisele Abena'),
    (1, 'Herve Bello'),
    (1, 'Iris Ndongo'),
    (1, 'Joel Kamdem'),
    (1, 'Karl Atangana'),
    (1, 'Laure Mvondo'),
    (1, 'Marcel Yonta'),
    (1, 'Nadia Essomba'),
    (1, 'Olivier Njonang'),
    (1, 'Pauline Ekani'),
    (1, 'Quentin Ngassa'),
    (1, 'Rachel Ondo'),
    (1, 'Serge Owona'),
    (1, 'Tania Biloa'),
    (1, 'Urbain Nkodo'),
    (1, 'Vanessa Etoundi'),
    (1, 'Wilfried Moukoko'),
    (1, 'Xavier Mballa'),
    (1, 'Yvonne Soppo'),
    (1, 'Zacharie Ndiaye'),
    (1, 'Aurelie Fouda'),
    (1, 'Brice Etoa'),
    (1, 'Clarisse Manga'),
    (1, 'Didier Ntamack');

-- 2) Session passée : ouverte hier, code expiré (RG1), puis clôturée (RG12).
--    Code fixe sans caractères ambigus (pas de 0/O/1/I).
INSERT INTO session (promotion_id, titre, code, ouverture_at, expiration_at, cloture_at)
VALUES (1, 'TP relecture — semaine passee', 'PAST99',
        TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP),
        TIMESTAMPADD(MINUTE, 75, TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP)),
        TIMESTAMPADD(MINUTE, 90, TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP)));

-- 3) Les 30 étudiants présents à PAST99 : 20 ont marqué eux-mêmes (source ETUDIANT,
--    ~40 min après l'ouverture), 10 ont été ajoutés par le formateur (source FORMATEUR,
--    RG11/Q14 — « ça se voit » dans le tableau).
INSERT INTO presence (session_id, etudiant_id, source, marque_at)
SELECT 2, id, 'ETUDIANT', TIMESTAMPADD(MINUTE, -1400, CURRENT_TIMESTAMP)
FROM etudiant WHERE id <= 20;

INSERT INTO presence (session_id, etudiant_id, source, marque_at)
SELECT 2, id, 'FORMATEUR', TIMESTAMPADD(MINUTE, -1390, CURRENT_TIMESTAMP)
FROM etudiant WHERE id BETWEEN 21 AND 30;

-- 4) Huit exercices déposés sur PAST99 (RG5 : un par étudiant) — ids 1 à 8
INSERT INTO exercice (session_id, etudiant_id, lien, statut, depose_at, maj_at) VALUES
    (2, 1, 'https://github.com/demo/exo-aline',   'relu',    TIMESTAMPADD(MINUTE, -1380, CURRENT_TIMESTAMP), NULL),
    (2, 2, 'https://github.com/demo/exo-boris',   'relu',    TIMESTAMPADD(MINUTE, -1378, CURRENT_TIMESTAMP), NULL),
    (2, 3, 'https://github.com/demo/exo-cynthia', 'relu',    TIMESTAMPADD(MINUTE, -1376, CURRENT_TIMESTAMP), NULL),
    (2, 4, 'https://github.com/demo/exo-david',   'relu',    TIMESTAMPADD(MINUTE, -1374, CURRENT_TIMESTAMP), NULL),
    (2, 5, 'https://github.com/demo/exo-emma',    'relu',    TIMESTAMPADD(MINUTE, -1372, CURRENT_TIMESTAMP), NULL),
    (2, 6, 'https://github.com/demo/exo-frank',   'relu',    TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), NULL),
    (2, 7, 'https://github.com/demo/exo-gisele',  'relu',    TIMESTAMPADD(MINUTE, -1368, CURRENT_TIMESTAMP), NULL),
    (2, 8, 'https://github.com/demo/exo-herve',   'assigne', TIMESTAMPADD(MINUTE, -1366, CURRENT_TIMESTAMP), NULL);

-- 5) Relectures associées (RG16 : une seule par exercice ; RG7 : relecteur ≠ auteur).
--    Sept rendues (RG10 : définitives), une encore en attente pour l'étudiant 9
--    (visible dans l'écran relecteur, Q11).
INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, assignee_at, rendue_at) VALUES
    (1, 2, 'rendue', 15, 'Justifications claires, quelques oublis sur le cas limite.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1310, CURRENT_TIMESTAMP)),
    (2, 3, 'rendue', 12, 'Bon principe, mais le tri n''est pas expliqué.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1305, CURRENT_TIMESTAMP)),
    (3, 1, 'rendue', 17, 'Très bon découpage, tests pertinents.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1300, CURRENT_TIMESTAMP)),
    (4, 5, 'rendue', 9, 'Le code compile mais la boucle est infinie sur liste vide.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1295, CURRENT_TIMESTAMP)),
    (5, 4, 'rendue', 14, 'Solution correcte, nommage à revoir.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1290, CURRENT_TIMESTAMP)),
    (6, 7, 'rendue', 18, 'Exemplaire : lisible, testé, documenté.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1285, CURRENT_TIMESTAMP)),
    (7, 6, 'rendue', 11, 'La complexité dépasse le sujet, à simplifier.', TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1280, CURRENT_TIMESTAMP)),
    (8, 9, 'assignee', NULL, NULL, TIMESTAMPADD(MINUTE, -1370, CURRENT_TIMESTAMP), NULL);

-- 6) Deux présences déjà posées sur la session ouverte DEMO24 (ids 4 et 5) :
--    le tableau de la session en cours n'est pas vide, et les 28 autres étudiants
--    peuvent encore marquer leur présence en direct avec le code DEMO24.
INSERT INTO presence (session_id, etudiant_id, source, marque_at) VALUES
    (1, 4, 'ETUDIANT', CURRENT_TIMESTAMP),
    (1, 5, 'ETUDIANT', CURRENT_TIMESTAMP);
