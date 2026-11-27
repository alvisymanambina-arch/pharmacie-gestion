package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Categorie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategorieDAO implements BaseDAO<Categorie> {

    private final DatabaseConfig databaseConfig;

    public CategorieDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Categorie findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCategorie(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategorieDAO] Error finding category by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Categorie> findAll() {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapResultSetToCategorie(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[CategorieDAO] Error finding all categories: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger les catégories depuis la base.", e);
        }
        return categories;
    }

    @Override
    public boolean insert(Categorie entity) {
        String sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getDescription());
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
            System.err.println("[CategorieDAO] Error inserting category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Categorie entity) {
        String sql = "UPDATE categories SET nom = ?, description = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getDescription());
            statement.setInt(3, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[CategorieDAO] Error updating category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[CategorieDAO] Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Categorie findByName(String nom) {
        String sql = "SELECT * FROM categories WHERE nom = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nom);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCategorie(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategorieDAO] Error finding category by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Categorie mapResultSetToCategorie(ResultSet resultSet) throws SQLException {
        Categorie categorie = new Categorie();
        categorie.setId(resultSet.getInt("id"));
        categorie.setNom(resultSet.getString("nom"));
        categorie.setDescription(resultSet.getString("description"));
        return categorie;
    }
}
