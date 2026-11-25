package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Approvisionnement;
import com.pharmacie.models.ApprovisionnementDetail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApprovisionnementDAO {

    private final DatabaseConfig databaseConfig;

    public ApprovisionnementDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    /**
     * Insère un approvisionnement avec ses détails via une transaction.
     * Met à jour le stock des médicaments (augmentation) et le prix d'achat.
     */
    public boolean insert(Approvisionnement approvisionnement) {
        Connection connection = null;
        try {
            validateApprovisionnement(approvisionnement);
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            // 1. Insérer dans approvisionnements
            String sqlAppro = "INSERT INTO approvisionnements (fournisseur_id, "
                    + "utilisateur_id, date_appro, montant_total) VALUES (?, ?, ?, ?)";
            int approId;
            try (PreparedStatement statement = connection.prepareStatement(
                    sqlAppro, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, approvisionnement.getFournisseurId());
                statement.setInt(2, approvisionnement.getUtilisateurId());
                LocalDateTime dateAppro = approvisionnement.getDateAppro() != null
                        ? approvisionnement.getDateAppro() : LocalDateTime.now();
                statement.setTimestamp(3, Timestamp.valueOf(dateAppro));
                statement.setBigDecimal(4, approvisionnement.getMontantTotal());
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        approId = generatedKeys.getInt(1);
                        approvisionnement.setId(approId);
                    } else {
                        throw new SQLException("Impossible de récupérer l'ID de l'approvisionnement.");
                    }
                }
            }

            // 2 & 3. Insérer les détails et augmenter le stock
            String sqlDetail = "INSERT INTO approvisionnement_details "
                    + "(approvisionnement_id, medicament_id, quantite, prix_achat, sous_total) "
                    + "VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE medicaments SET quantite_stock = quantite_stock + ?, "
                    + "prix_achat = ? WHERE id = ?";
                String sqlLot = "INSERT INTO lots_medicaments (medicament_id, numero_lot, date_expiration, "
                    + "quantite_initiale, quantite_disponible, prix_achat) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement detailStatement = connection.prepareStatement(sqlDetail);
                 PreparedStatement stockStatement = connection.prepareStatement(sqlUpdateStock);
                 PreparedStatement lotStatement = connection.prepareStatement(sqlLot)) {
                for (ApprovisionnementDetail detail : approvisionnement.getDetails()) {
                    detailStatement.setInt(1, approId);
                    detailStatement.setInt(2, detail.getMedicamentId());
                    detailStatement.setInt(3, detail.getQuantite());
                    detailStatement.setBigDecimal(4, detail.getPrixAchat());
                    detailStatement.setBigDecimal(5, detail.getSousTotal());
                    detailStatement.executeUpdate();

                    lotStatement.setInt(1, detail.getMedicamentId());
                    lotStatement.setString(2, detail.getNumeroLot());
                    lotStatement.setDate(3, java.sql.Date.valueOf(detail.getDateExpiration()));
                    lotStatement.setInt(4, detail.getQuantite());
                    lotStatement.setInt(5, detail.getQuantite());
                    lotStatement.setBigDecimal(6, detail.getPrixAchat());
                    lotStatement.executeUpdate();

                    // Augmenter le stock et mettre à jour le prix d'achat
                    stockStatement.setInt(1, detail.getQuantite());
                    stockStatement.setBigDecimal(2, detail.getPrixAchat());
                    stockStatement.setInt(3, detail.getMedicamentId());
                    if (stockStatement.executeUpdate() != 1) {
                        throw new SQLException("Médicament introuvable pour l'ID: "
                                + detail.getMedicamentId());
                    }
                }
            }

                    try (PreparedStatement auditStatement = connection.prepareStatement(
                        "INSERT INTO journal_audit (utilisateur_id, action, entite, entite_id, details) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                    auditStatement.setInt(1, approvisionnement.getUtilisateurId());
                    auditStatement.setString(2, "CREATION");
                    auditStatement.setString(3, "APPROVISIONNEMENT");
                    auditStatement.setInt(4, approId);
                    auditStatement.setString(5, "Approvisionnement valide; montant="
                        + approvisionnement.getMontantTotal() + "; articles="
                        + approvisionnement.getDetails().size());
                    auditStatement.executeUpdate();
                    }

            connection.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[ApprovisionnementDAO] Erreur insertion (rollback): " + e.getMessage());
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[ApprovisionnementDAO] Erreur rollback: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("[ApprovisionnementDAO] Erreur fermeture: " + e.getMessage());
                }
            }
        }
    }

    private void validateApprovisionnement(Approvisionnement approvisionnement) throws SQLException {
        if (approvisionnement == null || approvisionnement.getFournisseurId() <= 0
                || approvisionnement.getUtilisateurId() <= 0) {
            throw new SQLException("Approvisionnement, fournisseur ou utilisateur invalide.");
        }

        List<ApprovisionnementDetail> details = approvisionnement.getDetails() == null
                ? Collections.emptyList() : approvisionnement.getDetails();
        if (details.isEmpty()) {
            throw new SQLException("Un approvisionnement doit contenir au moins un article.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ApprovisionnementDetail detail : details) {
            if (detail == null || detail.getMedicamentId() <= 0 || detail.getQuantite() <= 0
                    || detail.getPrixAchat() == null
                    || detail.getPrixAchat().compareTo(BigDecimal.ZERO) < 0
                    || detail.getNumeroLot() == null || detail.getNumeroLot().isBlank()
                    || detail.getDateExpiration() == null
                    || !detail.getDateExpiration().isAfter(LocalDate.now())) {
                throw new SQLException("Détail d'approvisionnement invalide.");
            }
            BigDecimal sousTotal = detail.getPrixAchat()
                    .multiply(BigDecimal.valueOf(detail.getQuantite()));
            detail.setSousTotal(sousTotal);
            total = total.add(sousTotal);
        }
        approvisionnement.setMontantTotal(total);
    }

    public List<Approvisionnement> findAll() {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        String sql = "SELECT a.*, f.nom AS fournisseur_nom "
                + "FROM approvisionnements a "
                + "LEFT JOIN fournisseurs f ON a.fournisseur_id = f.id "
                + "ORDER BY a.date_appro DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Approvisionnement appro = mapResultSetToApprovisionnement(resultSet);
                appro.setDetails(findDetailsByApprovisionnementId(appro.getId()));
                approvisionnements.add(appro);
            }
        } catch (SQLException e) {
            System.err.println("[ApprovisionnementDAO] Error findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return approvisionnements;
    }

    public List<Approvisionnement> findByFournisseur(int fournisseurId) {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        String sql = "SELECT a.*, f.nom AS fournisseur_nom "
                + "FROM approvisionnements a "
                + "LEFT JOIN fournisseurs f ON a.fournisseur_id = f.id "
                + "WHERE a.fournisseur_id = ? ORDER BY a.date_appro DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fournisseurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Approvisionnement appro = mapResultSetToApprovisionnement(resultSet);
                    appro.setDetails(findDetailsByApprovisionnementId(appro.getId()));
                    approvisionnements.add(appro);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ApprovisionnementDAO] Error findByFournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        return approvisionnements;
    }

    public List<Approvisionnement> findByDateRange(LocalDate debut, LocalDate fin) {
        List<Approvisionnement> approvisionnements = new ArrayList<>();
        String sql = "SELECT a.*, f.nom AS fournisseur_nom "
                + "FROM approvisionnements a "
                + "LEFT JOIN fournisseurs f ON a.fournisseur_id = f.id "
                + "WHERE a.date_appro BETWEEN ? AND ? "
                + "ORDER BY a.date_appro DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(debut.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Approvisionnement appro = mapResultSetToApprovisionnement(resultSet);
                    appro.setDetails(findDetailsByApprovisionnementId(appro.getId()));
                    approvisionnements.add(appro);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ApprovisionnementDAO] Error findByDateRange: " + e.getMessage());
            e.printStackTrace();
        }
        return approvisionnements;
    }

    private List<ApprovisionnementDetail> findDetailsByApprovisionnementId(int approId) {
        List<ApprovisionnementDetail> details = new ArrayList<>();
        String sql = "SELECT ad.*, m.nom AS medicament_nom "
                + "FROM approvisionnement_details ad "
                + "LEFT JOIN medicaments m ON ad.medicament_id = m.id "
                + "WHERE ad.approvisionnement_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, approId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(mapResultSetToApprovisionnementDetail(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ApprovisionnementDAO] Error findDetails: " + e.getMessage());
        }
        return details;
    }

    private Approvisionnement mapResultSetToApprovisionnement(ResultSet resultSet) throws SQLException {
        Approvisionnement appro = new Approvisionnement();
        appro.setId(resultSet.getInt("id"));
        Timestamp ts = resultSet.getTimestamp("date_appro");
        if (ts != null) {
            appro.setDateAppro(ts.toLocalDateTime());
        }
        appro.setFournisseurId(resultSet.getInt("fournisseur_id"));
        appro.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        appro.setMontantTotal(resultSet.getBigDecimal("montant_total"));
        try {
            appro.setFournisseurNom(resultSet.getString("fournisseur_nom"));
        } catch (SQLException ignored) {
            // colonne optionnelle
        }
        return appro;
    }

    private ApprovisionnementDetail mapResultSetToApprovisionnementDetail(ResultSet resultSet) throws SQLException {
        ApprovisionnementDetail detail = new ApprovisionnementDetail();
        detail.setId(resultSet.getInt("id"));
        detail.setApprovisionnementId(resultSet.getInt("approvisionnement_id"));
        detail.setMedicamentId(resultSet.getInt("medicament_id"));
        detail.setQuantite(resultSet.getInt("quantite"));
        detail.setPrixAchat(resultSet.getBigDecimal("prix_achat"));
        detail.setSousTotal(resultSet.getBigDecimal("sous_total"));
        try {
            detail.setMedicamentNom(resultSet.getString("medicament_nom"));
        } catch (SQLException ignored) {
            // colonne optionnelle
        }
        return detail;
    }
}
