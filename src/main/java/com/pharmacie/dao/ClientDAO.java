package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO implements BaseDAO<Client> {

    private final DatabaseConfig databaseConfig;

    public ClientDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Client findById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToClient(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error finding client by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clients.add(mapResultSetToClient(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error finding all clients: " + e.getMessage());
            e.printStackTrace();
        }
        return clients;
    }

    @Override
    public boolean insert(Client entity) {
        String sql = "INSERT INTO clients (nom, prenom, telephone, email, adresse) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getPrenom());
            statement.setString(3, entity.getTelephone());
            statement.setString(4, entity.getEmail());
            statement.setString(5, entity.getAdresse());
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
            System.err.println("[ClientDAO] Error inserting client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Client entity) {
        String sql = "UPDATE clients SET nom = ?, prenom = ?, telephone = ?, email = ?, adresse = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getPrenom());
            statement.setString(3, entity.getTelephone());
            statement.setString(4, entity.getEmail());
            statement.setString(5, entity.getAdresse());
            statement.setInt(6, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error updating client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error deleting client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Client> searchByNom(String keyword) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE nom LIKE ? OR prenom LIKE ? ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            statement.setString(2, "%" + keyword + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    clients.add(mapResultSetToClient(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error searching clients by name: " + e.getMessage());
            e.printStackTrace();
        }
        return clients;
    }

    private Client mapResultSetToClient(ResultSet resultSet) throws SQLException {
        Client client = new Client();
        client.setId(resultSet.getInt("id"));
        client.setNom(resultSet.getString("nom"));
        client.setPrenom(resultSet.getString("prenom"));
        client.setTelephone(resultSet.getString("telephone"));
        client.setEmail(resultSet.getString("email"));
        client.setAdresse(resultSet.getString("adresse"));
        return client;
    }
}
