
USE pharmacie_db;

UPDATE categories
SET nom = CONVERT(0x566974616d696e6573206574204d696ec3a972617578 USING utf8mb4),
    description = CONVERT(0x436f6d706cc3a96d656e747320766974616d696e6971756573206574206d696ec3a972617578 USING utf8mb4)
WHERE id = 48;

INSERT INTO medicaments (nom, dosage, forme, description, categorie_id, prix_achat, prix_vente,
                         quantite_stock, seuil_alerte, date_expiration, code_barre)
SELECT source.nom, source.dosage, source.forme, NULL, source.categorie_id,
       ROUND(source.prix_vente * 0.70, 2), source.prix_vente, source.stock, 10, NULL, NULL
FROM (
    SELECT 'Metformine' nom, '500 mg' dosage, 'Comprimé' forme, 16 categorie_id, 700 prix_vente, 150 stock
    UNION ALL SELECT 'Metformine', '850 mg', 'Comprimé', 16, 900, 120
    UNION ALL SELECT 'Gliclazide', '80 mg', 'Comprimé', 16, 1200, 70
    UNION ALL SELECT 'Glibenclamide', '5 mg', 'Comprimé', 16, 600, 65
    UNION ALL SELECT 'Insuline humaine', '100 UI/ml', 'Injectable', 16, 18000, 25
    UNION ALL SELECT 'Lopéramide', '2 mg', 'Gélule', 10, 800, 90
    UNION ALL SELECT 'SRO', '20,5 g', 'Sachet', 10, 1000, 200
    UNION ALL SELECT 'Métoclopramide', '10 mg', 'Comprimé', 15, 700, 60
    UNION ALL SELECT 'Ondansétron', '4 mg', 'Comprimé', 15, 2000, 35
    UNION ALL SELECT 'Oméprazole', '20 mg', 'Gélule', 13, 1000, 120
    UNION ALL SELECT 'Pantoprazole', '40 mg', 'Comprimé', 13, 1500, 90
    UNION ALL SELECT 'Famotidine', '20 mg', 'Comprimé', 13, 1000, 70
    UNION ALL SELECT 'Fer', '100 mg', 'Comprimé', 28, 700, 100
    UNION ALL SELECT 'Acide folique', '5 mg', 'Comprimé', 28, 500, 120
    UNION ALL SELECT 'Amoxicilline pédiatrique', '250 mg/5 ml', 'Suspension', 31, 4500, 50
    UNION ALL SELECT 'Paracétamol pédiatrique', '2,4 %', 'Sirop', 31, 4000, 80
    UNION ALL SELECT 'Ibuprofène pédiatrique', '100 mg/5 ml', 'Suspension', 31, 4500, 60
) source
WHERE NOT EXISTS (
    SELECT 1 FROM medicaments m
    WHERE m.nom = source.nom AND COALESCE(m.dosage, '') = COALESCE(source.dosage, '')
      AND COALESCE(m.forme, '') = COALESCE(source.forme, '')
);

INSERT INTO lots_medicaments (medicament_id, numero_lot, date_expiration,
                              quantite_initiale, quantite_disponible, prix_achat)
SELECT m.id, CONCAT('DEMO-', m.id), '2027-12-31', m.quantite_stock, m.quantite_stock, m.prix_achat
FROM medicaments m
WHERE m.quantite_stock > 0
  AND NOT EXISTS (SELECT 1 FROM lots_medicaments l WHERE l.medicament_id = m.id);
