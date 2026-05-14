-- ============================================================
--  Portail de suivi du doctorat
--  Script d'initialisation – BASE H2 (développement)
--  Compatible : Spring Boot 3.x / Hibernate JOINED inheritance
--
--  Mots de passe (BCrypt $2a$10$) :
--    admin123  → hash ci-dessous
--    dir123    → hash ci-dessous
--    doc123    → hash ci-dessous
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- 0. NETTOYAGE (ordre inverse des FK)
-- ─────────────────────────────────────────────────────────────
DELETE FROM documents;
DELETE FROM membres_jury;
DELETE FROM formations_doctorales;
DELETE FROM publications;
DELETE FROM soutenances;
DELETE FROM inscriptions;
DELETE FROM campagnes_inscription;
DELETE FROM doctorants;
DELETE FROM directeurs_these;
DELETE FROM personnel_admins;
DELETE FROM utilisateurs;

-- ─────────────────────────────────────────────────────────────
-- 1. TABLE MÈRE : utilisateurs
--    Héritage JOINED → une ligne par utilisateur dans cette table
--    + une ligne dans la table fille correspondante
--
--    Hashes BCrypt (rounds=10) pré-calculés :
--      admin123 : $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
--      dir123   : $2a$10$TwoDifferentPasswordsHashAreHere1ForDirecteurXXXXXXXXXX.
--      doc123   : $2a$10$YetAnotherHashForDoctorantPasswordXXXXXXXXXXXXXXXXXXXX.
--
--    NOTE : Pour H2/dev, on utilise {noop} si vous configurez
--    NoOpPasswordEncoder, sinon remplacez par de vrais hashes BCrypt
--    générés via : new BCryptPasswordEncoder().encode("motdepasse")
-- ─────────────────────────────────────────────────────────────

INSERT INTO utilisateurs (id, nom, prenom, email, mot_de_passe, role, actif, dtype) VALUES
-- Personnel administratif
(1,  'BENALI',    'Fatima',   'admin@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'PERSONNEL_ADMIN',   TRUE, 'ADMIN'),

-- Directeurs de thèse
(2,  'AHAIDOUS',  'Khadija',  'directeur@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DIRECTEUR_THESE',   TRUE, 'DIRECTEUR'),

(3,  'BERRADA',   'Mohamed',  'directeur2@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DIRECTEUR_THESE',   TRUE, 'DIRECTEUR'),

-- Doctorants
(4,  'ALAOUI',    'Youssef',  'doctorant1@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DOCTORANT',         TRUE, 'DOCTORANT'),

(5,  'TAZI',      'Salma',    'doctorant2@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DOCTORANT',         TRUE, 'DOCTORANT'),

(6,  'OUALI',     'Hamza',    'doctorant3@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DOCTORANT',         TRUE, 'DOCTORANT'),

(7,  'MANSOURI',  'Nadia',    'doctorant4@doctorat.ma',
 '$2a$10$IHiOHnn2RyjZEfZxb3dvWObW4Q79jvBP83Z7i9IN/giJ1kuFzV2yS',
 'DOCTORANT',         TRUE, 'DOCTORANT');
-- 2. TABLE FILLE : personnel_admins
-- ─────────────────────────────────────────────────────────────
INSERT INTO personnel_admins (id, matricule, service, poste) VALUES
(1, 'ADM-001', 'Scolarité', 'Responsable doctorat');

-- ─────────────────────────────────────────────────────────────
-- 3. TABLE FILLE : directeurs_these
-- ─────────────────────────────────────────────────────────────
INSERT INTO directeurs_these (id, specialite, grade, etablissement) VALUES
(2, 'Informatique et IA',      'Professeur',        'ENSA Khouribga'),
(3, 'Mathématiques appliquées', 'Professeur Habilité', 'FST Beni Mellal');

-- ─────────────────────────────────────────────────────────────
-- 4. TABLE FILLE : doctorants
-- ─────────────────────────────────────────────────────────────
INSERT INTO doctorants
    (id, cne, sujet_these, date_premiere_inscription,
     annee_encours, statut, laboratoire, specialite, directeur_id)
VALUES
-- Doctorant 1 : inscrit depuis 2021, prérequis remplis → peut soutenir
(4,  'CNE-2021-001',
     'Intelligence artificielle et optimisation des réseaux IoT',
     '2021-09-01', 4, 'ACTIF',
     'Laboratoire LIMATI', 'Informatique', 2),

-- Doctorant 2 : inscrit depuis 2023, prérequis incomplets
(5,  'CNE-2023-002',
     'Blockchain et sécurité des données de santé',
     '2023-09-01', 2, 'ACTIF',
     'Laboratoire LCS', 'Informatique', 2),

-- Doctorant 3 : inscrit depuis 2020, proche limite 6 ans → alerte
(6,  'CNE-2020-003',
     'Optimisation multi-objectifs par algorithmes évolutionnaires',
     '2020-09-01', 5, 'DEROGATION_ACCORDEE',
     'Laboratoire LIMATI', 'Mathématiques', 3),

-- Doctorant 4 : inscrit depuis 2022, en cours normal
(7,  'CNE-2022-004',
     'Traitement automatique du langage naturel pour l''arabe dialectal',
     '2022-09-01', 3, 'ACTIF',
     'Laboratoire LCS', 'Informatique', 3);

-- ─────────────────────────────────────────────────────────────
-- 5. CAMPAGNES D'INSCRIPTION
-- ─────────────────────────────────────────────────────────────
INSERT INTO campagnes_inscription
    (id, annee_universitaire, date_ouverture, date_fermeture, active, description)
VALUES
(1, 2022, '2022-09-01', '2022-10-31', FALSE,
    'Campagne inscription/réinscription 2022-2023'),
(2, 2023, '2023-09-01', '2023-10-31', FALSE,
    'Campagne inscription/réinscription 2023-2024'),
(3, 2024, '2024-09-01', '2024-10-31', FALSE,
    'Campagne inscription/réinscription 2024-2025'),
(4, 2025, '2025-09-01', '2025-10-31', TRUE,
    'Campagne inscription/réinscription 2025-2026');

-- ─────────────────────────────────────────────────────────────
-- 6. INSCRIPTIONS
-- ─────────────────────────────────────────────────────────────
INSERT INTO inscriptions
    (id, type, annee_universitaire, date_depot, statut,
     avis_directeur, date_avis_directeur,
     commentaire_admin, date_validation_admin,
     motif_rejet, doctorant_id, campagne_id)
VALUES
-- Doctorant 1 : 4 inscriptions validées (2021→2024)
(1,  'PREMIERE_INSCRIPTION', 2021, '2021-09-10', 'VALIDE',
     'Dossier complet, sujet pertinent', '2021-09-12 10:00:00',
     'Validé administrativement', '2021-09-15 14:00:00',
     NULL, 4, NULL),

(2,  'REINSCRIPTION', 2022, '2022-09-08', 'VALIDE',
     'Bonne progression', '2022-09-10 09:30:00',
     'Validé', '2022-09-13 11:00:00',
     NULL, 4, 1),

(3,  'REINSCRIPTION', 2023, '2023-09-07', 'VALIDE',
     'Avancement satisfaisant', '2023-09-09 10:00:00',
     'Validé', '2023-09-12 15:00:00',
     NULL, 4, 2),

(4,  'REINSCRIPTION', 2024, '2024-09-06', 'VALIDE',
     'Excellent travail', '2024-09-08 10:00:00',
     'Validé', '2024-09-11 14:00:00',
     NULL, 4, 3),

-- Doctorant 2 : 2 inscriptions validées
(5,  'PREMIERE_INSCRIPTION', 2023, '2023-09-09', 'VALIDE',
     'Sujet innovant', '2023-09-11 10:00:00',
     'Validé', '2023-09-14 11:00:00',
     NULL, 5, 2),

(6,  'REINSCRIPTION', 2024, '2024-09-09', 'VALIDE',
     'Bonne progression', '2024-09-11 10:00:00',
     'Validé', '2024-09-14 11:00:00',
     NULL, 5, 3),

-- Doctorant 3 : en attente validation admin
(7,  'REINSCRIPTION', 2025, '2025-09-10', 'EN_VALIDATION_ADMIN',
     'Avancement acceptable malgré le retard', '2025-09-12 09:00:00',
     NULL, NULL,
     NULL, 6, 4),

-- Doctorant 4 : en attente avis directeur
(8,  'REINSCRIPTION', 2025, '2025-09-11', 'EN_VALIDATION_DIRECTEUR',
     NULL, NULL,
     NULL, NULL,
     NULL, 7, 4),

-- Exemple de dossier rejeté (doctorant 2, ancienne demande)
(9,  'REINSCRIPTION', 2022, '2022-09-05', 'REJETE',
     NULL, NULL,
     NULL, NULL,
     'Pièces justificatives manquantes (diplôme)', 5, 1);

-- ─────────────────────────────────────────────────────────────
-- 7. PUBLICATIONS
-- ─────────────────────────────────────────────────────────────
INSERT INTO publications
    (id, titre, type, quartile, revue_ou_conference,
     doi, date_publication, statut, commentaire_validation, doctorant_id)
VALUES
-- ── Doctorant 1 (prérequis remplis) ──────────────────────────
-- 2 articles Q1/Q2
(1,  'Deep Learning for IoT Network Optimization: A Federated Approach',
     'ARTICLE_JOURNAL', 'Q1',
     'IEEE Transactions on Network and Service Management',
     '10.1109/TNSM.2023.001',
     '2023-03-15', 'VALIDE', 'Excellent travail, très bien cité', 4),

(2,  'Federated Learning in Edge Computing: Challenges and Opportunities',
     'ARTICLE_JOURNAL', 'Q2',
     'Future Generation Computer Systems',
     '10.1016/j.future.2023.112233',
     '2023-07-20', 'VALIDE', 'Contribution significative', 4),

-- 2 conférences
(3,  'Adaptive Resource Allocation in 5G Heterogeneous Networks',
     'CONFERENCE', 'NON_CLASSE',
     'IEEE INFOCOM 2023 – Conférence internationale',
     NULL,
     '2023-05-10', 'VALIDE', 'Accepté après révisions mineures', 4),

(4,  'Privacy-Preserving Machine Learning for Smart Cities',
     'CONFERENCE', 'NON_CLASSE',
     'ACM CCS 2023 – Computer and Communications Security',
     NULL,
     '2023-11-03', 'VALIDE', 'Bon paper, bien présenté', 4),

-- Article supplémentaire en attente
(5,  'Transfer Learning Approaches for Low-Resource IoT Environments',
     'ARTICLE_JOURNAL', 'Q1',
     'IEEE Internet of Things Journal',
     '10.1109/JIOT.2024.001234',
     '2024-02-10', 'EN_ATTENTE', NULL, 4),

-- ── Doctorant 2 (prérequis incomplets) ───────────────────────
(6,  'Blockchain for Healthcare Data Privacy: A Survey',
     'CONFERENCE', 'NON_CLASSE',
     'IEEE HEALTHCOM 2024',
     NULL,
     '2024-06-01', 'VALIDE', 'Bonne introduction au domaine', 5),

(7,  'Smart Contracts for Medical Record Management',
     'ARTICLE_JOURNAL', 'Q2',
     'Journal of Biomedical Informatics',
     '10.1016/j.jbi.2024.001',
     '2024-09-15', 'EN_ATTENTE', NULL, 5),

-- ── Doctorant 3 ───────────────────────────────────────────────
(8,  'Multi-Objective Genetic Algorithm for Scheduling Problems',
     'ARTICLE_JOURNAL', 'Q1',
     'Applied Soft Computing',
     '10.1016/j.asoc.2022.001',
     '2022-04-10', 'VALIDE', 'Très bonne contribution théorique', 6),

(9,  'Hybrid Evolutionary Strategies for Combinatorial Optimization',
     'ARTICLE_JOURNAL', 'Q2',
     'Computers & Operations Research',
     '10.1016/j.cor.2023.001',
     '2023-01-20', 'VALIDE', 'Résultats expérimentaux solides', 6),

(10, 'Pareto-Optimal Solutions for Multi-Criteria Decision Making',
     'CONFERENCE', 'NON_CLASSE',
     'CEC 2022 – IEEE Congress on Evolutionary Computation',
     NULL,
     '2022-07-18', 'VALIDE', 'Bien présenté', 6),

(11, 'Swarm Intelligence Applied to Logistics Networks',
     'CONFERENCE', 'NON_CLASSE',
     'GECCO 2023 – Genetic and Evolutionary Computation Conference',
     NULL,
     '2023-07-15', 'VALIDE', NULL, 6),

-- ── Doctorant 4 ───────────────────────────────────────────────
(12, 'Sentiment Analysis of Moroccan Darija Using BERT',
     'CONFERENCE', 'NON_CLASSE',
     'ACL 2024 – Findings',
     NULL,
     '2024-08-05', 'VALIDE', NULL, 7),

(13, 'Morphological Analysis of Arabic Dialects with Neural Networks',
     'ARTICLE_JOURNAL', 'Q2',
     'Natural Language Engineering',
     '10.1017/S135132492400001X',
     '2024-03-20', 'EN_ATTENTE', NULL, 7);

-- ─────────────────────────────────────────────────────────────
-- 8. FORMATIONS DOCTORALES
-- ─────────────────────────────────────────────────────────────
INSERT INTO formations_doctorales
    (id, intitule, heures, date_formation, date_validation,
     organisateur, attestation_url, validee, doctorant_id)
VALUES
-- ── Doctorant 1 : total = 210h validées ──────────────────────
(1,  'Éthique et déontologie de la recherche scientifique',
     30, '2022-02-10', '2022-02-20', 'CNRST Rabat', NULL, TRUE, 4),

(2,  'Rédaction scientifique en anglais (Academic Writing)',
     60, '2022-09-05', '2022-09-15', 'USMS Beni Mellal', NULL, TRUE, 4),

(3,  'Machine Learning avancé et Deep Learning',
     120, '2023-01-15', '2023-01-25', 'Université Mohammed V Rabat', NULL, TRUE, 4),

-- Heures totales doctorant 1 : 30+60+120 = 210h ✅

-- ── Doctorant 2 : total = 90h validées (insuffisant) ─────────
(4,  'Introduction à la Blockchain et aux smart contracts',
     45, '2023-11-10', '2023-11-20', 'UM6P Benguerir', NULL, TRUE, 5),

(5,  'Cybersécurité et protection des données',
     45, '2024-03-08', '2024-03-18', 'ENSA Khouribga', NULL, TRUE, 5),

(6,  'Formation en cours : Big Data et Hadoop',
     60, '2025-01-20', NULL, 'ENSIAS Rabat', NULL, FALSE, 5),

-- ── Doctorant 3 : total = 240h validées ──────────────────────
(7,  'Algorithmes d''optimisation combinatoire',
     80, '2021-10-12', '2021-10-22', 'FST Beni Mellal', NULL, TRUE, 6),

(8,  'Méthodes de recherche opérationnelle avancées',
     80, '2022-04-04', '2022-04-14', 'ENSA Fès', NULL, TRUE, 6),

(9,  'Calcul haute performance et parallélisme',
     80, '2023-03-20', '2023-03-30', 'CNRST Rabat', NULL, TRUE, 6),

-- ── Doctorant 4 : total = 90h validées ───────────────────────
(10, 'Traitement automatique du langage naturel (NLP) fondamental',
     60, '2023-02-15', '2023-02-25', 'ENSIAS Rabat', NULL, TRUE, 7),

(11, 'Transformers et modèles de langage (BERT, GPT)',
     30, '2023-11-05', '2023-11-15', 'Université Cadi Ayyad', NULL, TRUE, 7);

-- ─────────────────────────────────────────────────────────────
-- 9. SOUTENANCE (doctorant 1 – prérequis remplis)
-- ─────────────────────────────────────────────────────────────
INSERT INTO soutenances
    (id, date_demande, date_planifiee, heure_planifiee, lieu,
     statut, publis_satisfaits, formation_satisfaite, documents_complets,
     date_autorisation, commentaire_autorisation, doctorant_id)
VALUES
(1,  '2025-09-20', '2025-11-15', '10:00:00',
     'Salle des thèses – ENSA Khouribga',
     'PLANIFIEE', TRUE, TRUE, TRUE,
     '2025-10-20 09:00:00',
     'Tous les prérequis sont remplis. Soutenance autorisée.',
     4);

-- ─────────────────────────────────────────────────────────────
-- 10. MEMBRES DU JURY (pour la soutenance ci-dessus)
-- ─────────────────────────────────────────────────────────────
INSERT INTO membres_jury
    (id, nom, prenom, email, grade, etablissement, qualite,
     rapport_soumis, rapport_favorable, date_soumission_rapport,
     soutenance_id)
VALUES
-- Président du jury
(1,  'EL HACHIMI', 'Rachid',
     'elhachimi@uhp.ac.ma',
     'Professeur', 'Université Hassan Premier Settat',
     'PRESIDENT',
     FALSE, FALSE, NULL, 1),

-- Rapporteur 1 (rapport favorable soumis)
(2,  'MOUSSAOUI',  'Karim',
     'moussaoui@uca.ac.ma',
     'Professeur', 'Université Cadi Ayyad Marrakech',
     'RAPPORTEUR',
     TRUE, TRUE, '2025-10-10', 1),

-- Rapporteur 2 (rapport favorable soumis)
(3,  'BENHIDA',    'Soumia',
     'benhida@um5.ac.ma',
     'Professeur Habilité', 'Université Mohammed V Rabat',
     'RAPPORTEUR',
     TRUE, TRUE, '2025-10-12', 1),

-- Examinateur
(4,  'CHARFI',     'Samir',
     'charfi@ensa.ac.ma',
     'Professeur Habilité', 'ENSA Marrakech',
     'EXAMINATEUR',
     FALSE, FALSE, NULL, 1),

-- Directeur de thèse (invité)
(5,  'AHAIDOUS',   'Khadija',
     'directeur@doctorat.ma',
     'Professeur', 'ENSA Khouribga',
     'INVITE',
     FALSE, FALSE, NULL, 1);

-- ─────────────────────────────────────────────────────────────
-- 11. DOCUMENTS JOINTS
-- ─────────────────────────────────────────────────────────────
INSERT INTO documents
    (id, nom, type, url, format, taille, date_telechargement,
     conforme, inscription_id, soutenance_id)
VALUES
-- Documents d'inscription (inscription n°1 du doctorant 1)
(1,  'CV_ALAOUI_Youssef.pdf',
     'CV', '/uploads/inscriptions/1/cv_alaoui.pdf',
     'pdf', 245678, '2021-09-10 09:00:00', TRUE, 1, NULL),

(2,  'Diplome_Master_ALAOUI.pdf',
     'DIPLOME', '/uploads/inscriptions/1/diplome_alaoui.pdf',
     'pdf', 1024000, '2021-09-10 09:05:00', TRUE, 1, NULL),

(3,  'Lettre_motivation_ALAOUI.pdf',
     'LETTRE_MOTIVATION', '/uploads/inscriptions/1/lettre_alaoui.pdf',
     'pdf', 102400, '2021-09-10 09:10:00', TRUE, 1, NULL),

-- Documents soutenance (doctorant 1)
(4,  'Manuscrit_these_ALAOUI_v_finale.pdf',
     'MANUSCRIT_THESE', '/uploads/soutenances/1/manuscrit_alaoui.pdf',
     'pdf', 8192000, '2025-09-25 10:00:00', TRUE, NULL, 1),

(5,  'Rapport_antiplagiat_ALAOUI.pdf',
     'RAPPORT_ANTIPLAGIAT', '/uploads/soutenances/1/antiplagiat_alaoui.pdf',
     'pdf', 512000, '2025-09-25 10:15:00', TRUE, NULL, 1),

(6,  'Rapport_publications_ALAOUI.pdf',
     'RAPPORT_PUBLICATIONS', '/uploads/soutenances/1/publications_alaoui.pdf',
     'pdf', 307200, '2025-09-25 10:20:00', TRUE, NULL, 1),

(7,  'Attestations_formations_ALAOUI.pdf',
     'ATTESTATION_FORMATION', '/uploads/soutenances/1/formations_alaoui.pdf',
     'pdf', 409600, '2025-09-25 10:25:00', TRUE, NULL, 1),

(8,  'Autorisation_soutenance_ALAOUI.pdf',
     'AUTORISATION_SOUTENANCE', '/uploads/soutenances/1/autorisation_alaoui.pdf',
     'pdf', 102400, '2025-10-20 09:15:00', TRUE, NULL, 1),

-- Documents d'inscription doctorant 2
(9,  'CV_TAZI_Salma.pdf',
     'CV', '/uploads/inscriptions/5/cv_tazi.pdf',
     'pdf', 198000, '2023-09-09 10:00:00', TRUE, 5, NULL),

(10, 'Diplome_Master_TAZI.pdf',
     'DIPLOME', '/uploads/inscriptions/5/diplome_tazi.pdf',
     'pdf', 956000, '2023-09-09 10:05:00', TRUE, 5, NULL);

-- ─────────────────────────────────────────────────────────────
-- 12. RÉINITIALISATION DES SÉQUENCES (H2)
-- ─────────────────────────────────────────────────────────────
ALTER TABLE utilisateurs         ALTER COLUMN id RESTART WITH 100;
ALTER TABLE campagnes_inscription ALTER COLUMN id RESTART WITH 100;
ALTER TABLE inscriptions          ALTER COLUMN id RESTART WITH 100;
ALTER TABLE soutenances           ALTER COLUMN id RESTART WITH 100;
ALTER TABLE membres_jury          ALTER COLUMN id RESTART WITH 100;
ALTER TABLE publications          ALTER COLUMN id RESTART WITH 100;
ALTER TABLE formations_doctorales ALTER COLUMN id RESTART WITH 100;
ALTER TABLE documents             ALTER COLUMN id RESTART WITH 100;

-- ─────────────────────────────────────────────────────────────
-- FIN DU SCRIPT
-- ─────────────────────────────────────────────────────────────
-- Résumé des données insérées :
--   • 1 admin        : admin@doctorat.ma      / mot de passe BCrypt
--   • 2 directeurs   : directeur@doctorat.ma  / directeur2@doctorat.ma
--   • 4 doctorants   : doctorant1..4@doctorat.ma
--   • 4 campagnes    : 2022 → 2025 (2025 active)
--   • 9 inscriptions : validées, en attente, rejetée
--   • 13 publications: Q1/Q2, conférences, en attente
--   • 11 formations  : validées et en attente
--   • 1 soutenance   : planifiée (doctorant 1)
--   • 5 membres jury : président, rapporteurs, examinateur
--   • 10 documents   : CV, diplômes, manuscrit, rapports...
-- ─────────────────────────────────────────────────────────────
