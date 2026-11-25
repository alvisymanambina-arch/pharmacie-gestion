-- PharmaGestion v2: run this once on an existing pharmacie_db database.
-- Take a backup before running it.
USE pharmacie_db;

ALTER TABLE medicaments ADD COLUMN dosage VARCHAR(100) AFTER nom;
ALTER TABLE medicaments ADD COLUMN forme VARCHAR(100) AFTER dosage;

CREATE TABLE IF NOT EXISTS lots_medicaments (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE vente_details ADD COLUMN lot_id INT NULL AFTER medicament_id;
ALTER TABLE vente_details ADD CONSTRAINT fk_vente_details_lot FOREIGN KEY (lot_id)
    REFERENCES lots_medicaments(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE vente_details ADD KEY idx_vente_details_lot (lot_id);

ALTER TABLE ventes ADD COLUMN mode_paiement ENUM('ESPECES','MOBILE_MONEY','CARTE','CREDIT')
    NOT NULL DEFAULT 'ESPECES' AFTER remise;
ALTER TABLE ventes ADD COLUMN statut ENUM('VALIDEE','ANNULEE') NOT NULL DEFAULT 'VALIDEE'
    AFTER mode_paiement;

CREATE TABLE IF NOT EXISTS journal_audit (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Existing stock predates batch tracking. Create one provisional lot per product so sales
-- keep working; replace its number/date with the real delivery information afterwards.
INSERT INTO lots_medicaments (medicament_id, numero_lot, date_expiration, quantite_initiale,
                              quantite_disponible, prix_achat)
SELECT m.id, CONCAT('INITIAL-', m.id), COALESCE(m.date_expiration, '2099-12-31'),
       m.quantite_stock, m.quantite_stock, m.prix_achat
FROM medicaments m
WHERE m.quantite_stock > 0
  AND NOT EXISTS (SELECT 1 FROM lots_medicaments l WHERE l.medicament_id = m.id);
