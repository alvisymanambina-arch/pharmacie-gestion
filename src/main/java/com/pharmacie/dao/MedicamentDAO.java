package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Medicament;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAO implements BaseDAO<Medicament> {

    private final DatabaseConfig databaseConfig;

    public MedicamentDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Medicament findById(int id) {
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "WHERE m.id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToMedicament(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding medicament by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Medicament> findAll() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "ORDER BY m.nom ASC, m.dosage ASC, m.forme ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding all medicaments: " + e.getMessage());
            e.printStackTrace();
        }
        return medicaments;
    }

    @Override
    public boolean insert(Medicament entity) {
        String sql = "INSERT INTO medicaments (nom, dosage, forme, description, categorie_id, "
                + "prix_achat, prix_vente, quantite_stock, seuil_alerte, "
                + "date_expiration, code_barre) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getDosage());
            statement.setString(3, entity.getForme());
            statement.setString(4, entity.getDescription());
            if (entity.getCategorieId() > 0) {
                statement.setInt(5, entity.getCategorieId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            statement.setBigDecimal(6, entity.getPrixAchat());
            statement.setBigDecimal(7, entity.getPrixVente());
            statement.setInt(8, entity.getQuantiteStock());
            statement.setInt(9, entity.getSeuilAlerte());
            if (entity.getDateExpiration() != null) {
                statement.setDate(10, java.sql.Date.valueOf(entity.getDateExpiration()));
            } else {
                statement.setNull(10, java.sql.Types.DATE);
            }
            statement.setString(11, entity.getCodeBarre());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error inserting medicament: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Medicament entity) {
        String sql = "UPDATE medicaments SET nom = ?, dosage = ?, forme = ?, description = ?, categorie_id = ?, "
                + "prix_achat = ?, prix_vente = ?, quantite_stock = ?, seuil_alerte = ?, "
                + "date_expiration = ?, code_barre = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getDosage());
            statement.setString(3, entity.getForme());
            statement.setString(4, entity.getDescription());
            if (entity.getCategorieId() > 0) {
                statement.setInt(5, entity.getCategorieId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            statement.setBigDecimal(6, entity.getPrixAchat());
            statement.setBigDecimal(7, entity.getPrixVente());
            statement.setInt(8, entity.getQuantiteStock());
            statement.setInt(9, entity.getSeuilAlerte());
            if (entity.getDateExpiration() != null) {
                statement.setDate(10, java.sql.Date.valueOf(entity.getDateExpiration()));
            } else {
                statement.setNull(10, java.sql.Types.DATE);
            }
            statement.setString(11, entity.getCodeBarre());
            statement.setInt(12, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error updating medicament: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM medicaments WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error deleting medicament: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Medicament> searchByNom(String keyword) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "WHERE m.nom LIKE ? OR m.code_barre LIKE ? ORDER BY m.nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicaments.add(mapResultSetToMedicament(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error searching medicaments by name: " + e.getMessage());
            e.printStackTrace();
        }
        return medicaments;
    }

    public List<Medicament> findByCategorie(int categorieId) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "WHERE m.categorie_id = ? ORDER BY m.nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categorieId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicaments.add(mapResultSetToMedicament(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding medicaments by category: " + e.getMessage());
            e.printStackTrace();
        }
        return medicaments;
    }

    public List<Medicament> findStockFaible() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "WHERE m.quantite_stock <= m.seuil_alerte ORDER BY m.quantite_stock ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding low stock medicaments: " + e.getMessage());
            e.printStackTrace();
        }
        return medicaments;
    }

    public List<Medicament> findExpirationsProches() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT m.*, c.nom AS categorie_nom FROM medicaments m "
                + "LEFT JOIN categories c ON m.categorie_id = c.id "
                + "WHERE m.date_expiration BETWEEN CURRENT_DATE "
                + "AND CURRENT_DATE + INTERVAL '30 days' "
                + "ORDER BY m.date_expiration ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding near-expiration medicaments: " + e.getMessage());
            e.printStackTrace();
        }
        return medicaments;
    }

    public boolean updateStock(int medicamentId, int quantite) {
        String sql = "UPDATE medicaments SET quantite_stock = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantite);
            statement.setInt(2, medicamentId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error updating stock: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStockAfterVente(int medicamentId, int quantiteVendue) {
        String sql = "UPDATE medicaments SET quantite_stock = quantite_stock - ? "
                + "WHERE id = ? AND quantite_stock >= ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantiteVendue);
            statement.setInt(2, medicamentId);
            statement.setInt(3, quantiteVendue);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error updating stock after vente: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Medicament mapResultSetToMedicament(ResultSet resultSet) throws SQLException {
        Medicament medicament = new Medicament();
        medicament.setId(resultSet.getInt("id"));
        medicament.setNom(resultSet.getString("nom"));
        medicament.setDosage(resultSet.getString("dosage"));
        medicament.setForme(resultSet.getString("forme"));
        medicament.setDescription(resultSet.getString("description"));
        int categorieId = resultSet.getInt("categorie_id");
        medicament.setCategorieId(resultSet.wasNull() ? 0 : categorieId);
        medicament.setPrixAchat(resultSet.getBigDecimal("prix_achat"));
        medicament.setPrixVente(resultSet.getBigDecimal("prix_vente"));
        medicament.setQuantiteStock(resultSet.getInt("quantite_stock"));
        medicament.setSeuilAlerte(resultSet.getInt("seuil_alerte"));
        java.sql.Date dateExp = resultSet.getDate("date_expiration");
        if (dateExp != null) {
            medicament.setDateExpiration(dateExp.toLocalDate());
        }
        medicament.setCodeBarre(resultSet.getString("code_barre"));
        try {
            medicament.setCategorieNom(resultSet.getString("categorie_nom"));
        } catch (SQLException ignored) {
            // colonne categorie_nom peut ne pas être présente dans toutes les requêtes
        }
        return medicament;
    }
}
