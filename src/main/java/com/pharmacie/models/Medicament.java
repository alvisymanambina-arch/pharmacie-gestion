package com.pharmacie.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicament {

    private int id;
    private String nom;
    private String dosage;
    private String forme;
    private String description;
    private int categorieId;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private int quantiteStock;
    private int seuilAlerte;
    private LocalDate dateExpiration;
    private String codeBarre;

    // Champs supplémentaires pour l'affichage
    private String categorieNom;

    public Medicament() {
    }

    public Medicament(String nom, String description, int categorieId, BigDecimal prixAchat,
                      BigDecimal prixVente, int quantiteStock, int seuilAlerte,
                      LocalDate dateExpiration, String codeBarre) {
        this.nom = nom;
        this.description = description;
        this.categorieId = categorieId;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
        this.dateExpiration = dateExpiration;
        this.codeBarre = codeBarre;
    }

    public Medicament(int id, String nom, String description, int categorieId,
                      BigDecimal prixAchat, BigDecimal prixVente, int quantiteStock,
                      int seuilAlerte, LocalDate dateExpiration, String codeBarre) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.categorieId = categorieId;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
        this.dateExpiration = dateExpiration;
        this.codeBarre = codeBarre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { this.prixVente = prixVente; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public int getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public String getCodeBarre() { return codeBarre; }
    public void setCodeBarre(String codeBarre) { this.codeBarre = codeBarre; }

    public String getCategorieNom() { return categorieNom; }
    public void setCategorieNom(String categorieNom) { this.categorieNom = categorieNom; }

    public boolean isStockFaible() {
        return quantiteStock <= seuilAlerte;
    }

    public boolean isExpiré() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return nom + (codeBarre != null ? " [" + codeBarre + "]" : "");
    }
}
