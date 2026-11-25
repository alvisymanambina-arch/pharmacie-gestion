-- ============================================================
-- Script de creation de la base de donnees pharmacie_db
-- SGBD : MySQL
-- Encodage : UTF-8
-- ============================================================

CREATE DATABASE IF NOT EXISTS pharmacie_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacie_db;

-- Table : utilisateurs
CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'PHARMACIEN') NOT NULL DEFAULT 'PHARMACIEN',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : categories
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : medicaments
CREATE TABLE medicaments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    forme VARCHAR(100),
    description TEXT,
    categorie_id INT NOT NULL,
    prix_achat DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    prix_vente DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantite_stock INT NOT NULL DEFAULT 0,
    seuil_alerte INT NOT NULL DEFAULT 10,
    date_expiration DATE,
    code_barre VARCHAR(50) UNIQUE,
    CONSTRAINT fk_medicaments_categorie
        FOREIGN KEY (categorie_id) REFERENCES categories(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lots_medicaments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicament_id INT NOT NULL,
    numero_lot VARCHAR(80) NOT NULL,
    date_expiration DATE NOT NULL,
    quantite_initiale INT NOT NULL,
    quantite_disponible INT NOT NULL,
    prix_achat DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    date_reception DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_lot_medicament (medicament_id, numero_lot),
    KEY idx_lots_fefo (medicament_id, date_expiration, quantite_disponible),
    CONSTRAINT fk_lots_medicament FOREIGN KEY (medicament_id) REFERENCES medicaments(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_lot_quantites CHECK (quantite_initiale >= 0 AND quantite_disponible >= 0
        AND quantite_disponible <= quantite_initiale)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : fournisseurs
CREATE TABLE fournisseurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100),
    contact_person VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : clients
CREATE TABLE clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20),
    email VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : ventes
CREATE TABLE ventes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT,
    utilisateur_id INT NOT NULL,
    date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    remise DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    mode_paiement ENUM('ESPECES','MOBILE_MONEY','CARTE','CREDIT') NOT NULL DEFAULT 'ESPECES',
    statut ENUM('VALIDEE','ANNULEE') NOT NULL DEFAULT 'VALIDEE',
    CONSTRAINT fk_ventes_client
        FOREIGN KEY (client_id) REFERENCES clients(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_ventes_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table : vente_details
-- ============================================================
CREATE TABLE vente_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vente_id INT NOT NULL,
    medicament_id INT NOT NULL,
    lot_id INT NULL,
    quantite INT NOT NULL DEFAULT 1,
    prix_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sous_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_vente_details_vente
        FOREIGN KEY (vente_id) REFERENCES ventes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_vente_details_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicaments(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_vente_details_lot
        FOREIGN KEY (lot_id) REFERENCES lots_medicaments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : approvisionnements
CREATE TABLE approvisionnements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fournisseur_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    date_appro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_approvisionnements_fournisseur
        FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_approvisionnements_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : approvisionnement_details
CREATE TABLE approvisionnement_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    approvisionnement_id INT NOT NULL,
    medicament_id INT NOT NULL,
    quantite INT NOT NULL DEFAULT 1,
    prix_achat DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sous_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_appro_details_approvisionnement
        FOREIGN KEY (approvisionnement_id) REFERENCES approvisionnements(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_appro_details_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicaments(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE journal_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NULL,
    action VARCHAR(60) NOT NULL,
    entite VARCHAR(60) NOT NULL,
    entite_id INT NULL,
    details TEXT NULL,
    date_action DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_entite (entite, entite_id),
    KEY idx_audit_date (date_action),
    CONSTRAINT fk_audit_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Donnees par defaut

-- Utilisateur admin par defaut (mot de passe : admin123)
INSERT IGNORE INTO utilisateurs (nom, prenom, username, password, role, actif)
VALUES ('Administrateur', 'Systeme', 'admin',
        '$2a$10$Q06G3J347DgSkFFZimc/leCTCnOe9EFqtjxKMezsAYbg/jgmGgBGe',
        'ADMIN', TRUE);

-- Categories par defaut
INSERT IGNORE INTO categories (nom, description) VALUES
('Antalgiques', 'Medicaments pour soulager la douleur'),
('Anti-inflammatoires', 'Medicaments pour reduire les inflammations'),
('Antibiotiques', 'Medicaments pour combattre les infections bacteriennes'),
('Antiseptiques', 'Produits pour la desinfection et l''antisepsie'),
('Antipyrétiques', 'Medicaments pour faire baisser la fievre'),
('Antitussifs', 'Medicaments pour calmer la toux'),
('Expectorants', 'Medicaments facilitant l''elimination des secretions bronchiques'),
('Antihistaminiques', 'Medicaments contre les allergies'),
('Antidiarrheiques', 'Medicaments utilises contre la diarrhee'),
('Laxatifs', 'Medicaments facilitant le transit intestinal'),
('Antiacides', 'Medicaments reduisant l''acidite gastrique'),
('Antiulcereux', 'Medicaments utilises dans le traitement des ulceres gastriques'),
('Antispasmodiques', 'Medicaments soulageant les spasmes et douleurs abdominales'),
('Antiemetiques', 'Medicaments contre les nausees et vomissements'),
('Antidiabetiques', 'Medicaments utilises pour controler la glycemie'),
('Antihypertenseurs', 'Medicaments utilises pour traiter l''hypertension'),
('Cardiovasculaires', 'Medicaments destines au traitement des maladies cardiovasculaires'),
('Vitamine et Minéraux', 'Compléments vitaminiques et minéraux'),
('Antifongiques', 'Medicaments contre les infections causees par des champignons'),
('Antiviraux', 'Medicaments utilises contre certaines infections virales'),
('Antiparasitaires', 'Medicaments contre les parasites'),
('Dermatologiques', 'Medicaments et produits destines aux affections de la peau'),
('Ophtalmiques', 'Medicaments et produits destines aux soins des yeux'),
('ORL', 'Medicaments destines aux affections de l''oreille, du nez et de la gorge'),
('Respiratoires', 'Medicaments utilises pour les maladies respiratoires'),
('Gynecologiques', 'Medicaments destines aux affections gynecologiques'),
('Urologiques', 'Medicaments destines aux affections de l''appareil urinaire'),
('Contraceptifs', 'Produits utilises pour prevenir une grossesse'),
('Pediatriques', 'Medicaments destines principalement aux enfants'),
('Produits de premiers soins', 'Produits utilises pour les soins et traitements courants');
