package com.pharmacie.models;

import java.time.LocalDateTime;

public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String username;
    private String password;
    private String role;
    private boolean actif;
    private LocalDateTime dateCreation;

    public Utilisateur() {
    }

    public Utilisateur(String nom, String prenom, String username,
                       String password, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
        this.role = role;
        this.actif = true;
    }

    public Utilisateur(int id, String nom, String prenom, String username,
                       String password, String role, boolean actif,
                       LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
        this.role = role;
        this.actif = actif;
        this.dateCreation = dateCreation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + username + ")";
    }
}
