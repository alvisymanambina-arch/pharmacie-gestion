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

    private static final String SELECT_MEDICAMENTS =
            "SELECT m.*, c.nom AS categorie_nom, "
            + "COALESCE((SELECT SUM(l.quantite_disponible) FROM lots_medicaments l "
            + "WHERE l.medicament_id = m.id AND l.quantite_disponible > 0 "
            + "AND l.date_expiration >= CURRENT_DATE), 0) AS stock_disponible, "
            + "COALESCE((SELECT MIN(l.date_expiration) FROM lots_medicaments l "
            + "WHERE l.medicament_id = m.id AND l.quantite_disponible > 0), "
            + "m.date_expiration) AS prochaine_expiration "
            + "FROM medicaments m LEFT JOIN categories c ON m.categorie_id = c.id";

    private final DatabaseConfig databaseConfig;

    public MedicamentDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Medicament findById(int id) {
        String sql = SELECT_MEDICAMENTS + " WHERE m.id = ?";
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
            throw new IllegalStateException("Impossible de charger le médicament depuis la base.", e);
        }
        return null;
    }

    @Override
    public List<Medicament> findAll() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = SELECT_MEDICAMENTS + " ORDER BY m.nom ASC, m.dosage ASC, m.forme ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding all medicaments: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger les médicaments depuis la base.", e);
        }
        return medicaments;
    }

    @Override
    public boolean insert(Medicament entity) {
        String sql = "INSERT INTO medicaments (nom, dosage, forme, description, categorie_id, "
                + "prix_achat, prix_vente, seuil_alerte, code_barre) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            statement.setInt(8, entity.getSeuilAlerte());
            statement.setString(9, entity.getCodeBarre());

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
                + "prix_achat = ?, prix_vente = ?, seuil_alerte = ?, code_barre = ? WHERE id = ?";
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
            statement.setInt(8, entity.getSeuilAlerte());
            statement.setString(9, entity.getCodeBarre());
            statement.setInt(10, entity.getId());
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
        String sql = SELECT_MEDICAMENTS
                + " WHERE m.nom ILIKE ? OR COALESCE(m.code_barre, '') ILIKE ? "
                + "OR COALESCE(m.dosage, '') ILIKE ? OR COALESCE(m.forme, '') ILIKE ? "
                + "OR COALESCE(c.nom, '') ILIKE ? ORDER BY m.nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 5; i++) {
                statement.setString(i, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicaments.add(mapResultSetToMedicament(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error searching medicaments by name: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de rechercher les médicaments dans la base.", e);
        }
        return medicaments;
    }

    public List<Medicament> findByCategorie(int categorieId) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = SELECT_MEDICAMENTS + " WHERE m.categorie_id = ? ORDER BY m.nom ASC";
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
            throw new IllegalStateException("Impossible de filtrer les médicaments dans la base.", e);
        }
        return medicaments;
    }

    public List<Medicament> findStockFaible() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = SELECT_MEDICAMENTS
                + " WHERE COALESCE((SELECT SUM(l.quantite_disponible) FROM lots_medicaments l "
                + "WHERE l.medicament_id = m.id AND l.quantite_disponible > 0 "
                + "AND l.date_expiration >= CURRENT_DATE), 0) <= m.seuil_alerte "
                + "ORDER BY stock_disponible ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding low stock medicaments: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger les alertes de stock.", e);
        }
        return medicaments;
    }

    public List<Medicament> findExpirationsProches() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = SELECT_MEDICAMENTS
                + " WHERE COALESCE((SELECT MIN(l.date_expiration) FROM lots_medicaments l "
                + "WHERE l.medicament_id = m.id AND l.quantite_disponible > 0), m.date_expiration) "
                + "<= CURRENT_DATE + INTERVAL '30 days' "
                + "ORDER BY prochaine_expiration ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicaments.add(mapResultSetToMedicament(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[MedicamentDAO] Error finding near-expiration medicaments: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger les alertes d'expiration.", e);
        }
        return medicaments;
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
        int stockDisponible;
        try {
            stockDisponible = resultSet.getInt("stock_disponible");
        } catch (SQLException ignored) {
            stockDisponible = resultSet.getInt("quantite_stock");
        }
        medicament.setQuantiteStock(stockDisponible);
        medicament.setSeuilAlerte(resultSet.getInt("seuil_alerte"));
        java.sql.Date dateExp;
        try {
            dateExp = resultSet.getDate("prochaine_expiration");
        } catch (SQLException ignored) {
            dateExp = null;
        }
        if (dateExp == null) {
            dateExp = resultSet.getDate("date_expiration");
        }
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
