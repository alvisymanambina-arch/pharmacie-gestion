package com.pharmacie.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Vente {

    private int id;
    private int clientId;
    private int utilisateurId;
    private LocalDateTime dateVente;
    private BigDecimal montantTotal;
    private BigDecimal remise;

    // Champs supplémentaires
    private List<VenteDetail> details;
    private String clientNom;
    private String utilisateurNom;

    public Vente() {
        this.details = new ArrayList<>();
        this.remise = BigDecimal.ZERO;
        this.montantTotal = BigDecimal.ZERO;
    }

    public Vente(int clientId, int utilisateurId, BigDecimal montantTotal,
                 BigDecimal remise) {
        this.clientId = clientId;
        this.utilisateurId = utilisateurId;
        this.montantTotal = montantTotal;
        this.remise = remise;
        this.details = new ArrayList<>();
    }

    public Vente(int id, int clientId, int utilisateurId, LocalDateTime dateVente,
                 BigDecimal montantTotal, BigDecimal remise) {
        this.id = id;
        this.clientId = clientId;
        this.utilisateurId = utilisateurId;
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.remise = remise;
        this.details = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public LocalDateTime getDateVente() { return dateVente; }
    public void setDateVente(LocalDateTime dateVente) { this.dateVente = dateVente; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public BigDecimal getRemise() { return remise; }
    public void setRemise(BigDecimal remise) { this.remise = remise; }

    public List<VenteDetail> getDetails() { return details; }
    public void setDetails(List<VenteDetail> details) { this.details = details; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public void addDetail(VenteDetail detail) {
        this.details.add(detail);
    }

    public void removeDetail(VenteDetail detail) {
        this.details.remove(detail);
    }

    public int getNombreArticles() {
        return details.stream().mapToInt(VenteDetail::getQuantite).sum();
    }

    public BigDecimal calculerTotal() {
        return details.stream()
                .map(VenteDetail::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Vente #" + id + " - " + (clientNom != null ? clientNom : "Client anonyme")
                + " - " + montantTotal + " Ar";
    }
}
