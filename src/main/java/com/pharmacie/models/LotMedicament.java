package com.pharmacie.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A traceable batch of one medicine. Sales use the earliest expiring available batch (FEFO). */
public class LotMedicament {
    private int id;
    private int medicamentId;
    private String numeroLot;
    private LocalDate dateExpiration;
    private int quantiteInitiale;
    private int quantiteDisponible;
    private BigDecimal prixAchat;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }
    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public int getQuantiteInitiale() { return quantiteInitiale; }
    public void setQuantiteInitiale(int quantiteInitiale) { this.quantiteInitiale = quantiteInitiale; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(int quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }
}
