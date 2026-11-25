package com.pharmacie.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Approvisionnement {

    private int id;
    private int fournisseurId;
    private int utilisateurId;
    private LocalDateTime dateAppro;
    private BigDecimal montantTotal;

    // Champs supplémentaires
    private List<ApprovisionnementDetail> details;
    private String fournisseurNom;
    private String utilisateurNom;

    public Approvisionnement() {
        this.details = new ArrayList<>();
        this.montantTotal = BigDecimal.ZERO;
    }

    public Approvisionnement(int fournisseurId, int utilisateurId,
                             BigDecimal montantTotal) {
        this.fournisseurId = fournisseurId;
        this.utilisateurId = utilisateurId;
        this.montantTotal = montantTotal;
        this.details = new ArrayList<>();
    }

    public Approvisionnement(int id, int fournisseurId, int utilisateurId,
                             LocalDateTime dateAppro, BigDecimal montantTotal) {
        this.id = id;
        this.fournisseurId = fournisseurId;
        this.utilisateurId = utilisateurId;
        this.dateAppro = dateAppro;
        this.montantTotal = montantTotal;
        this.details = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(int fournisseurId) { this.fournisseurId = fournisseurId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public LocalDateTime getDateAppro() { return dateAppro; }
    public void setDateAppro(LocalDateTime dateAppro) { this.dateAppro = dateAppro; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public List<ApprovisionnementDetail> getDetails() { return details; }
    public void setDetails(List<ApprovisionnementDetail> details) { this.details = details; }

    public String getFournisseurNom() { return fournisseurNom; }
    public void setFournisseurNom(String fournisseurNom) { this.fournisseurNom = fournisseurNom; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public void addDetail(ApprovisionnementDetail detail) {
        this.details.add(detail);
    }

    public void removeDetail(ApprovisionnementDetail detail) {
        this.details.remove(detail);
    }

    public BigDecimal calculerTotal() {
        return details.stream()
                .map(ApprovisionnementDetail::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Approvisionnement #" + id + " - " + (fournisseurNom != null ? fournisseurNom : "Fournisseur #" + fournisseurId)
                + " - " + montantTotal + " Ar";
    }
}
