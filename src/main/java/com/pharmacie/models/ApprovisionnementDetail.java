package com.pharmacie.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ApprovisionnementDetail {

    private int id;
    private int approvisionnementId;
    private int medicamentId;
    private int quantite;
    private BigDecimal prixAchat;
    private BigDecimal sousTotal;
    private String numeroLot;
    private LocalDate dateExpiration;

    // Champ supplémentaire pour l'affichage
    private String medicamentNom;

    public ApprovisionnementDetail() {
    }

    public ApprovisionnementDetail(int medicamentId, int quantite,
                                   BigDecimal prixAchat, BigDecimal sousTotal) {
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixAchat = prixAchat;
        this.sousTotal = sousTotal;
    }

    public ApprovisionnementDetail(int id, int approvisionnementId, int medicamentId,
                                   int quantite, BigDecimal prixAchat, BigDecimal sousTotal) {
        this.id = id;
        this.approvisionnementId = approvisionnementId;
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixAchat = prixAchat;
        this.sousTotal = sousTotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getApprovisionnementId() { return approvisionnementId; }
    public void setApprovisionnementId(int approvisionnementId) { this.approvisionnementId = approvisionnementId; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    @Override
    public String toString() {
        return (medicamentNom != null ? medicamentNom : "Medicament #" + medicamentId)
                + " x" + quantite;
    }
}
