-- Donnees de demonstration: 100 medicaments.
-- Prix et stocks fictifs. Le prix d'achat est estime a 70% du prix de vente.
USE pharmacie_db;

INSERT IGNORE INTO categories (nom, description) VALUES
('Vitamines et Minéraux', 'Compléments vitaminiques et minéraux');

DROP TEMPORARY TABLE IF EXISTS demo_medicaments;
CREATE TEMPORARY TABLE demo_medicaments (
    nom VARCHAR(200) NOT NULL,
    categorie VARCHAR(100) NOT NULL,
    dosage VARCHAR(100),
    forme VARCHAR(100),
    prix_vente DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL
);

INSERT INTO demo_medicaments VALUES
('Paracétamol','Antalgiques','500 mg','Comprimé',500,250),
('Paracétamol','Antalgiques','1 g','Comprimé',800,180),
('Ibuprofène','Anti-inflammatoires','200 mg','Comprimé',700,150),
('Ibuprofène','Anti-inflammatoires','400 mg','Comprimé',1000,120),
('Aspirine','Antalgiques','500 mg','Comprimé',600,100),
('Diclofénac','Anti-inflammatoires','50 mg','Comprimé',900,110),
('Diclofénac','Anti-inflammatoires','75 mg','Gel',5500,45),
('Kétoprofène','Anti-inflammatoires','100 mg','Gélule',1200,80),
('Naproxène','Anti-inflammatoires','250 mg','Comprimé',1100,75),
('Tramadol','Antalgiques','50 mg','Gélule',1500,60),
('Amoxicilline','Antibiotiques','500 mg','Gélule',1200,100),
('Amoxicilline','Antibiotiques','1 g','Comprimé',1800,80),
('Amoxicilline + Acide clavulanique','Antibiotiques','500/125 mg','Comprimé',2500,70),
('Azithromycine','Antibiotiques','500 mg','Comprimé',3000,55),
('Ciprofloxacine','Antibiotiques','500 mg','Comprimé',1500,90),
('Doxycycline','Antibiotiques','100 mg','Gélule',1300,65),
('Métronidazole','Antibiotiques','500 mg','Comprimé',900,120),
('Cefixime','Antibiotiques','200 mg','Comprimé',2000,50),
('Ceftriaxone','Antibiotiques','1 g','Injectable',4500,35),
('Clindamycine','Antibiotiques','300 mg','Gélule',2200,45),
('Artéméther + Luméfantrine','Antiparasitaires','20/120 mg','Comprimé',2500,80),
('Albendazole','Antiparasitaires','400 mg','Comprimé',700,150),
('Mébendazole','Antiparasitaires','100 mg','Comprimé',500,130),
('Praziquantel','Antiparasitaires','600 mg','Comprimé',2000,40),
('Ivermectine','Antiparasitaires','6 mg','Comprimé',1000,60),
('Fluconazole','Antifongiques','150 mg','Gélule',1800,70),
('Clotrimazole','Antifongiques','1 %','Crème',3500,55),
('Nystatine','Antifongiques','100 000 UI','Comprimé',1200,60),
('Acyclovir','Antiviraux','200 mg','Comprimé',1500,45),
('Acyclovir','Antiviraux','5 %','Crème',4000,35),
('Cetirizine','Antihistaminiques','10 mg','Comprimé',800,120),
('Loratadine','Antihistaminiques','10 mg','Comprimé',900,100),
('Chlorphénamine','Antihistaminiques','4 mg','Comprimé',500,90),
('Salbutamol','Respiratoires','100 µg','Inhalateur',8500,40),
('Budésonide','Respiratoires','200 µg','Inhalateur',12000,25),
('Ambroxol','Antitussifs','30 mg','Comprimé',900,75),
('Acétylcystéine','Expectorants','200 mg','Sachet',1000,80),
('Dextrométhorphane','Antitussifs','15 mg','Sirop',4500,45),
('Guaifénésine','Expectorants','100 mg/5 ml','Sirop',5000,40),
('Bromhexine','Expectorants','4 mg/5 ml','Sirop',4000,50),
('Oméprazole','Antiulcéreux','20 mg','Gélule',1000,120),
('Pantoprazole','Antiulcéreux','40 mg','Comprimé',1500,90),
('Famotidine','Antiulcéreux','20 mg','Comprimé',1000,70),
('Hydroxyde d''aluminium + Magnésium','Antiacides','400/400 mg','Suspension',5500,40),
('Siméticone','Antispasmodiques','80 mg','Comprimé',900,80),
('Phloroglucinol','Antispasmodiques','80 mg','Comprimé',1000,65),
('Métoclopramide','Antiemétiques','10 mg','Comprimé',700,60),
('Ondansétron','Antiemétiques','4 mg','Comprimé',2000,35),
('Lopéramide','Antidiarrhéiques','2 mg','Gélule',800,90),
('SRO','Antidiarrhéiques','20,5 g','Sachet',1000,200),
('Lactulose','Laxatifs','667 mg/ml','Sirop',6500,35),
('Bisacodyl','Laxatifs','5 mg','Comprimé',600,100),
('Macrogol','Laxatifs','13,7 g','Sachet',1500,60),
('Metformine','Antidiabétiques','500 mg','Comprimé',700,150),
('Metformine','Antidiabétiques','850 mg','Comprimé',900,120),
('Gliclazide','Antidiabétiques','80 mg','Comprimé',1200,70),
('Glibenclamide','Antidiabétiques','5 mg','Comprimé',600,65),
('Insuline humaine','Antidiabétiques','100 UI/ml','Injectable',18000,25),
('Amlodipine','Antihypertenseurs','5 mg','Comprimé',600,180),
('Amlodipine','Antihypertenseurs','10 mg','Comprimé',800,130),
('Losartan','Antihypertenseurs','50 mg','Comprimé',1000,110),
('Losartan','Antihypertenseurs','100 mg','Comprimé',1500,75),
('Captopril','Antihypertenseurs','25 mg','Comprimé',500,100),
('Furosémide','Cardiovasculaires','40 mg','Comprimé',600,80),
('Atorvastatine','Cardiovasculaires','20 mg','Comprimé',1500,90),
('Bisoprolol','Cardiovasculaires','5 mg','Comprimé',1200,70),
('Amoxicilline pédiatrique','Pédiatriques','250 mg/5 ml','Suspension',4500,50),
('Paracétamol pédiatrique','Pédiatriques','2,4 %','Sirop',4000,80),
('Ibuprofène pédiatrique','Pédiatriques','100 mg/5 ml','Suspension',4500,60),
('Vitamine C','Vitamines et Minéraux','500 mg','Comprimé',700,150),
('Vitamine C','Vitamines et Minéraux','1 g','Comprimé effervescent',1200,100),
('Vitamine D3','Vitamines et Minéraux','1 000 UI','Gélule',1000,90),
('Fer + Acide folique','Vitamines et Minéraux','60/400 µg','Comprimé',900,100),
('Magnésium','Vitamines et Minéraux','300 mg','Comprimé',1200,80),
('Zinc','Vitamines et Minéraux','20 mg','Comprimé',800,100),
('Povidone iodée','Antiseptiques','10 %','Solution',6000,45),
('Chlorhexidine','Antiseptiques','0,05 %','Solution',4500,50),
('Alcool médical','Antiseptiques','70 %','Solution',3500,70),
('Eau oxygénée','Antiseptiques','10 volumes','Solution',2500,60),
('Sérum physiologique','Produits de premiers soins','0,9 %','Solution',1500,120),
('Acide fusidique','Dermatologiques','2 %','Crème',5500,40),
('Hydrocortisone','Dermatologiques','1 %','Crème',4000,45),
('Bétaméthasone','Dermatologiques','0,05 %','Crème',4500,35),
('Perméthrine','Dermatologiques','5 %','Crème',6000,30),
('Éconazole','Dermatologiques','1 %','Crème',4500,40),
('Chloramphénicol','Ophtalmiques','0,5 %','Collyre',4000,35),
('Tobramycine','Ophtalmiques','0,3 %','Collyre',5500,30),
('Larmes artificielles','Ophtalmiques','0,5 %','Collyre',6000,50),
('Gouttes auriculaires antiseptiques','ORL',NULL,'Gouttes',5000,30),
('Xylométazoline','ORL','0,1 %','Spray nasal',4500,45),
('Lévonorgestrel','Contraceptifs','1,5 mg','Comprimé',5000,30),
('Éthinylestradiol + Lévonorgestrel','Contraceptifs','0,03/0,15 mg','Comprimé',3500,50),
('Fer','Gynécologiques','100 mg','Comprimé',700,100),
('Acide folique','Gynécologiques','5 mg','Comprimé',500,120),
('Tamsulosine','Urologiques','0,4 mg','Gélule',1800,50),
('Finastéride','Urologiques','5 mg','Comprimé',1500,45),
('Vitamine B Complex','Vitamines et Minéraux',NULL,'Comprimé',800,100),
('Calcium + Vitamine D','Vitamines et Minéraux','500 mg/400 UI','Comprimé',1200,90),
('Multivitamines','Vitamines et Minéraux',NULL,'Comprimé',1500,75),
('Pansement antiseptique','Produits de premiers soins',NULL,'Pansement',1000,150);

INSERT INTO medicaments (nom, dosage, forme, description, categorie_id, prix_achat, prix_vente,
                         quantite_stock, seuil_alerte, date_expiration, code_barre)
SELECT d.nom, d.dosage, d.forme, NULL, c.id, ROUND(d.prix_vente * 0.70, 2), d.prix_vente,
       d.stock, 10, NULL, NULL
FROM demo_medicaments d
JOIN categories c ON c.nom = d.categorie
WHERE NOT EXISTS (
    SELECT 1 FROM medicaments m
    WHERE m.nom = d.nom AND COALESCE(m.dosage, '') = COALESCE(d.dosage, '')
      AND COALESCE(m.forme, '') = COALESCE(d.forme, '')
);

INSERT INTO lots_medicaments (medicament_id, numero_lot, date_expiration,
                              quantite_initiale, quantite_disponible, prix_achat)
SELECT m.id, CONCAT('DEMO-', m.id), '2027-12-31', m.quantite_stock, m.quantite_stock, m.prix_achat
FROM medicaments m
WHERE m.quantite_stock > 0
  AND NOT EXISTS (SELECT 1 FROM lots_medicaments l WHERE l.medicament_id = m.id);

DROP TEMPORARY TABLE demo_medicaments;
