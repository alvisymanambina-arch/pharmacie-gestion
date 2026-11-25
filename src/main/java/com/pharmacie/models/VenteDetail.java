package com.pharmacie.models;

import java.math.BigDecimal;

public class VenteDetail {

    private int id;
    private int venteId;
    private int medicamentId;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;

    // Champ supplémentaire pour l'affichage
    private String medicamentNom;

    public VenteDetail() {
    }

    public VenteDetail(int medicamentId, int quantite,
                        BigDecimal prixUnitaire, BigDecimal sousTotal) {
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
    }

    public VenteDetail(int id, int venteId, int medicamentId, int quantite,
                       BigDecimal prixUnitaire, BigDecimal sousTotal) {
        this.id = id;
        this.venteId = venteId;
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVenteId() { return venteId; }
    public void setVenteId(int venteId) { this.venteId = venteId; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    @Override
    public String toString() {
        return (medicamentNom != null ? medicamentNom : "Medicament #" + medicamentId)
                + " x" + quantite;
    }
}
