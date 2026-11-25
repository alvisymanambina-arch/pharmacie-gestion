package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Vente;
import com.pharmacie.models.VenteDetail;

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

public class VenteDAO {

    private final DatabaseConfig databaseConfig;

    public VenteDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    /**
     * Insère une vente avec ses détails via une transaction.
     */
    public boolean insert(Vente vente) {
        Connection connection = null;
        try {
            validateVente(vente);
            connection = databaseConfig.getConnection();
            connection.setAutoCommit(false);

            // 1. Insérer dans ventes
            String sqlVente = "INSERT INTO ventes (date_vente, client_id, utilisateur_id, "
                    + "montant_total, remise) VALUES (?, ?, ?, ?, ?)";
            int venteId;
            try (PreparedStatement statement = connection.prepareStatement(
                    sqlVente, Statement.RETURN_GENERATED_KEYS)) {
                LocalDateTime dateVente = vente.getDateVente() != null
                        ? vente.getDateVente() : LocalDateTime.now();
                statement.setTimestamp(1, Timestamp.valueOf(dateVente));

                if (vente.getClientId() > 0) {
                    statement.setInt(2, vente.getClientId());
                } else {
                    statement.setNull(2, java.sql.Types.INTEGER);
                }
                statement.setInt(3, vente.getUtilisateurId());
                statement.setBigDecimal(4, vente.getMontantTotal());
                statement.setBigDecimal(5, vente.getRemise() != null
                        ? vente.getRemise() : BigDecimal.ZERO);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        venteId = generatedKeys.getInt(1);
                        vente.setId(venteId);
                    } else {
                        throw new SQLException("Impossible de récupérer l'ID de la vente.");
                    }
                }
            }

            // 2 & 3. Insérer les détails et diminuer le stock
            String sqlDetail = "INSERT INTO vente_details (vente_id, medicament_id, lot_id, "
                    + "quantite, prix_unitaire, sous_total) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE medicaments SET quantite_stock = quantite_stock - ? "
                    + "WHERE id = ? AND quantite_stock >= ?";
            String sqlLots = "SELECT id, quantite_disponible FROM lots_medicaments WHERE medicament_id = ? "
                    + "AND quantite_disponible > 0 AND date_expiration >= CURRENT_DATE "
                    + "ORDER BY date_expiration, id FOR UPDATE";
            String sqlUpdateLot = "UPDATE lots_medicaments SET quantite_disponible = quantite_disponible - ? "
                    + "WHERE id = ? AND quantite_disponible >= ?";
            try (PreparedStatement detailStatement = connection.prepareStatement(sqlDetail);
                 PreparedStatement stockStatement = connection.prepareStatement(sqlUpdateStock);
                 PreparedStatement lotsStatement = connection.prepareStatement(sqlLots);
                 PreparedStatement lotUpdateStatement = connection.prepareStatement(sqlUpdateLot)) {
                for (VenteDetail detail : vente.getDetails()) {
                    int remaining = detail.getQuantite();
                    lotsStatement.setInt(1, detail.getMedicamentId());
                    try (ResultSet lots = lotsStatement.executeQuery()) {
                        while (lots.next() && remaining > 0) {
                            int lotId = lots.getInt("id");
                            int quantity = Math.min(remaining, lots.getInt("quantite_disponible"));
                            lotUpdateStatement.setInt(1, quantity);
                            lotUpdateStatement.setInt(2, lotId);
                            lotUpdateStatement.setInt(3, quantity);
                            if (lotUpdateStatement.executeUpdate() != 1) throw new SQLException("Lot modifie par une autre caisse.");
                            detailStatement.setInt(1, venteId);
                            detailStatement.setInt(2, detail.getMedicamentId());
                            detailStatement.setInt(3, lotId);
                            detailStatement.setInt(4, quantity);
                            detailStatement.setBigDecimal(5, detail.getPrixUnitaire());
                            detailStatement.setBigDecimal(6, detail.getPrixUnitaire().multiply(BigDecimal.valueOf(quantity)));
                            detailStatement.executeUpdate();
                            remaining -= quantity;
                        }
                    }
                    if (remaining > 0) throw new SQLException("Stock sans lot valide pour medicament ID: " + detail.getMedicamentId());

                    // Diminuer le stock
                    stockStatement.setInt(1, detail.getQuantite());
                    stockStatement.setInt(2, detail.getMedicamentId());
                    stockStatement.setInt(3, detail.getQuantite());
                    int rowsUpdated = stockStatement.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new SQLException("Stock insuffisant pour le médicament ID: "
                                + detail.getMedicamentId());
                    }
                }
            }

            try (PreparedStatement auditStatement = connection.prepareStatement(
                    "INSERT INTO journal_audit (utilisateur_id, action, entite, entite_id, details) VALUES (?, ?, ?, ?, ?)") ) {
                auditStatement.setInt(1, vente.getUtilisateurId());
                auditStatement.setString(2, "CREATION");
                auditStatement.setString(3, "VENTE");
                auditStatement.setInt(4, venteId);
                auditStatement.setString(5, "Vente validee; montant=" + vente.getMontantTotal()
                        + "; articles=" + vente.getDetails().size());
                auditStatement.executeUpdate();
            }
            connection.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[VenteDAO] Erreur insertion vente (rollback): " + e.getMessage());
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[VenteDAO] Erreur rollback: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("[VenteDAO] Erreur fermeture: " + e.getMessage());
                }
            }
        }
    }

    private void validateVente(Vente vente) throws SQLException {
        if (vente == null || vente.getUtilisateurId() <= 0) {
            throw new SQLException("Vente ou utilisateur invalide.");
        }

        List<VenteDetail> details = vente.getDetails() == null
                ? Collections.emptyList() : vente.getDetails();
        if (details.isEmpty()) {
            throw new SQLException("Une vente doit contenir au moins un article.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (VenteDetail detail : details) {
            if (detail == null || detail.getMedicamentId() <= 0 || detail.getQuantite() <= 0
                    || detail.getPrixUnitaire() == null
                    || detail.getPrixUnitaire().compareTo(BigDecimal.ZERO) < 0) {
                throw new SQLException("Détail de vente invalide.");
            }
            BigDecimal sousTotal = detail.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(detail.getQuantite()));
            detail.setSousTotal(sousTotal);
            total = total.add(sousTotal);
        }

        BigDecimal remise = vente.getRemise() == null ? BigDecimal.ZERO : vente.getRemise();
        if (remise.compareTo(BigDecimal.ZERO) < 0
                || remise.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new SQLException("La remise doit être comprise entre 0 et 100 %.");
        }
        vente.setRemise(remise);
        vente.setMontantTotal(total.multiply(BigDecimal.ONE.subtract(
                remise.divide(BigDecimal.valueOf(100)))));
    }

    public List<Vente> findAll() {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT v.*, c.nom AS client_nom, c.prenom AS client_prenom "
                + "FROM ventes v LEFT JOIN clients c ON v.client_id = c.id "
                + "ORDER BY v.date_vente DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Vente vente = mapResultSetToVente(resultSet);
                vente.setDetails(findDetailsByVenteId(vente.getId()));
                ventes.add(vente);
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return ventes;
    }

    public List<Vente> findByDateRange(LocalDate debut, LocalDate fin) {
        List<Vente> ventes = new ArrayList<>();
        // Inclure toute la journée de fin
        String sql = "SELECT v.*, c.nom AS client_nom, c.prenom AS client_prenom "
                + "FROM ventes v LEFT JOIN clients c ON v.client_id = c.id "
                + "WHERE v.date_vente BETWEEN ? AND ? "
                + "ORDER BY v.date_vente DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(debut.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(fin.atTime(23, 59, 59)));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vente vente = mapResultSetToVente(resultSet);
                    vente.setDetails(findDetailsByVenteId(vente.getId()));
                    ventes.add(vente);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error findByDateRange: " + e.getMessage());
            e.printStackTrace();
        }
        return ventes;
    }

    public List<Vente> findByClient(int clientId) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT v.*, c.nom AS client_nom, c.prenom AS client_prenom "
                + "FROM ventes v LEFT JOIN clients c ON v.client_id = c.id "
                + "WHERE v.client_id = ? ORDER BY v.date_vente DESC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Vente vente = mapResultSetToVente(resultSet);
                    vente.setDetails(findDetailsByVenteId(vente.getId()));
                    ventes.add(vente);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error findByClient: " + e.getMessage());
            e.printStackTrace();
        }
        return ventes;
    }

    public BigDecimal getChiffreAffairesJour(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM ventes "
                + "WHERE date_vente >= ? AND date_vente < ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal result = resultSet.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error getChiffreAffairesJour: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getChiffreAffairesMois(int mois, int annee) {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM ventes "
                + "WHERE EXTRACT(MONTH FROM date_vente) = ? "
                + "AND EXTRACT(YEAR FROM date_vente) = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mois);
            statement.setInt(2, annee);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal result = resultSet.getBigDecimal(1);
                    return result != null ? result : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error getChiffreAffairesMois: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public int getNombreVentesJour(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM ventes WHERE date_vente >= ? AND date_vente < ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error getNombreVentesJour: " + e.getMessage());
        }
        return 0;
    }

    private List<VenteDetail> findDetailsByVenteId(int venteId) {
        List<VenteDetail> details = new ArrayList<>();
        String sql = "SELECT vd.*, m.nom AS medicament_nom FROM vente_details vd "
                + "LEFT JOIN medicaments m ON vd.medicament_id = m.id "
                + "WHERE vd.vente_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, venteId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(mapResultSetToVenteDetail(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VenteDAO] Error findDetailsByVenteId: " + e.getMessage());
        }
        return details;
    }

    private Vente mapResultSetToVente(ResultSet resultSet) throws SQLException {
        Vente vente = new Vente();
        vente.setId(resultSet.getInt("id"));
        Timestamp ts = resultSet.getTimestamp("date_vente");
        if (ts != null) {
            vente.setDateVente(ts.toLocalDateTime());
        }
        int clientId = resultSet.getInt("client_id");
        vente.setClientId(resultSet.wasNull() ? 0 : clientId);
        vente.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        vente.setMontantTotal(resultSet.getBigDecimal("montant_total"));
        BigDecimal remise = resultSet.getBigDecimal("remise");
        vente.setRemise(remise != null ? remise : BigDecimal.ZERO);
        try {
            String nom = resultSet.getString("client_nom");
            String prenom = resultSet.getString("client_prenom");
            if (nom != null) {
                vente.setClientNom((prenom != null ? prenom + " " : "") + nom);
            }
        } catch (SQLException ignored) {
            // colonne optionnelle
        }
        return vente;
    }

    private VenteDetail mapResultSetToVenteDetail(ResultSet resultSet) throws SQLException {
        VenteDetail detail = new VenteDetail();
        detail.setId(resultSet.getInt("id"));
        detail.setVenteId(resultSet.getInt("vente_id"));
        detail.setMedicamentId(resultSet.getInt("medicament_id"));
        detail.setQuantite(resultSet.getInt("quantite"));
        detail.setPrixUnitaire(resultSet.getBigDecimal("prix_unitaire"));
        detail.setSousTotal(resultSet.getBigDecimal("sous_total"));
        try {
            detail.setMedicamentNom(resultSet.getString("medicament_nom"));
        } catch (SQLException ignored) {
            // colonne optionnelle
        }
        return detail;
    }
}
