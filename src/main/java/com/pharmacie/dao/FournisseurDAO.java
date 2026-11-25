package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Fournisseur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO implements BaseDAO<Fournisseur> {

    private final DatabaseConfig databaseConfig;

    public FournisseurDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Fournisseur findById(int id) {
        String sql = "SELECT * FROM fournisseurs WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFournisseur(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FournisseurDAO] Error finding fournisseur by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Fournisseur> findAll() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                fournisseurs.add(mapResultSetToFournisseur(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[FournisseurDAO] Error finding all fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
        return fournisseurs;
    }

    @Override
    public boolean insert(Fournisseur entity) {
        String sql = "INSERT INTO fournisseurs (nom, adresse, telephone, email, contact_person) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getAdresse());
            statement.setString(3, entity.getTelephone());
            statement.setString(4, entity.getEmail());
            statement.setString(5, entity.getContactPerson());
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
            System.err.println("[FournisseurDAO] Error inserting fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Fournisseur entity) {
        String sql = "UPDATE fournisseurs SET nom = ?, adresse = ?, telephone = ?, "
                + "email = ?, contact_person = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getAdresse());
            statement.setString(3, entity.getTelephone());
            statement.setString(4, entity.getEmail());
            statement.setString(5, entity.getContactPerson());
            statement.setInt(6, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[FournisseurDAO] Error updating fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM fournisseurs WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[FournisseurDAO] Error deleting fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Fournisseur> searchByNom(String keyword) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE nom LIKE ? ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    fournisseurs.add(mapResultSetToFournisseur(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FournisseurDAO] Error searching fournisseurs by name: " + e.getMessage());
            e.printStackTrace();
        }
        return fournisseurs;
    }

    private Fournisseur mapResultSetToFournisseur(ResultSet resultSet) throws SQLException {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(resultSet.getInt("id"));
        fournisseur.setNom(resultSet.getString("nom"));
        fournisseur.setAdresse(resultSet.getString("adresse"));
        fournisseur.setTelephone(resultSet.getString("telephone"));
        fournisseur.setEmail(resultSet.getString("email"));
        fournisseur.setContactPerson(resultSet.getString("contact_person"));
        return fournisseur;
    }
}
