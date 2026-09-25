-- V9 : double relecture (changement de besoin étape 3, issue #36).
-- RG16 évolue : chaque exercice peut recevoir DEUX relectures distinctes
-- (moyenne des notes rendues ; une seule rendue = note PROVISOIRE).
-- La contrainte unique(exercice_id) de V6 est remplacée par
-- unique(exercice_id, relecteur_id) : une seule relecture par couple
-- (exercice, relecteur), deux relecteurs distincts possibles par exercice.
-- Compatible avec les données existantes : les lignes déjà en base n'ont
-- qu'une relecture par exercice, elles satisfont la nouvelle contrainte
-- telles quelles — aucune donnée n'est perdue, aucune migration
-- antérieure (V1–V8) n'est modifiée.

-- H2 rattache l'index de l'ancienne contrainte unique à la clé étrangère :
-- on démonte donc FK, contrainte unique et index résiduel, puis on remonte
-- le tout avec la nouvelle contrainte (exercice_id, relecteur_id).
ALTER TABLE relecture DROP CONSTRAINT fk_relecture_exercice;
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;
DROP INDEX IF EXISTS uk_relecture_exercice_index_a;
ALTER TABLE relecture
    ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
ALTER TABLE relecture
    ADD CONSTRAINT fk_relecture_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id);

-- Nouvel état d'exercice : relu_partiel (une relecture rendue sur deux,
-- note affichée provisoire). L'ancienne contrainte V5 ne connaissait que
-- depose / en_attente_relecteur / assigne / relu.
ALTER TABLE exercice DROP CONSTRAINT ck_exercice_statut;
ALTER TABLE exercice
    ADD CONSTRAINT ck_exercice_statut CHECK (statut IN
        ('depose', 'en_attente_relecteur', 'assigne', 'relu_partiel', 'relu'));

-- Enrichissement du seed (V8) pour la démo de la double relecture :
-- une seconde relecture rendue pour les exercices 1 à 6 (note définitive =
-- moyenne des deux) ; l'exercice 7 garde une seule relecture rendue → statut
-- relu_partiel et note PROVISOIRE à l'écran étudiant ; l'exercice 8 reçoit une
-- seconde AFFECTATION non rendue (l'étudiant 10 la voit dans son écran relecteur).
INSERT INTO relecture (exercice_id, relecteur_id, statut, note, commentaire, assignee_at, rendue_at) VALUES
    (1, 3, 'rendue', 13, 'D''accord avec le fond, voir la convention de nommage.', TIMESTAMPADD(MINUTE, -1365, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1300, CURRENT_TIMESTAMP)),
    (2, 4, 'rendue', 15, 'Le tri est en fait bien justifié, bon travail.', TIMESTAMPADD(MINUTE, -1364, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1299, CURRENT_TIMESTAMP)),
    (3, 5, 'rendue', 14, 'Solide, la doc mériterait un exemple.', TIMESTAMPADD(MINUTE, -1363, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1298, CURRENT_TIMESTAMP)),
    (4, 6, 'rendue', 10, 'La boucle infinie est confirmée, voir l''index de fin.', TIMESTAMPADD(MINUTE, -1362, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1297, CURRENT_TIMESTAMP)),
    (5, 7, 'rendue', 16, 'Lisible en effet, le renommage proposé aide beaucoup.', TIMESTAMPADD(MINUTE, -1361, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1296, CURRENT_TIMESTAMP)),
    (6, 8, 'rendue', 17, 'Rien à redire sur la seconde lecture.', TIMESTAMPADD(MINUTE, -1360, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -1295, CURRENT_TIMESTAMP)),
    (8, 10, 'assignee', NULL, NULL, TIMESTAMPADD(MINUTE, -1359, CURRENT_TIMESTAMP), NULL);

UPDATE exercice SET statut = 'relu_partiel' WHERE id = 7;
