package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO implements BaseDAO<Utilisateur> {

    private final DatabaseConfig databaseConfig;

    public UtilisateurDAO() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUtilisateur(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error finding utilisateur by id: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger cet utilisateur depuis la base.", e);
        }
        return null;
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error finding all utilisateurs: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de charger les utilisateurs depuis la base.", e);
        }
        return utilisateurs;
    }

    @Override
    public boolean insert(Utilisateur entity) {
        if (entity == null || entity.getPassword() == null || entity.getPassword().isBlank()) {
            return false;
        }
        String sql = "INSERT INTO utilisateurs (nom, prenom, username, password, role, actif) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getPrenom());
            statement.setString(3, entity.getUsername());
            // Si le mot de passe est déjà hashé (commence par $2a), on l'utilise tel quel
            String password = entity.getPassword();
            if (password != null && !password.isEmpty() && !password.startsWith("$2")) {
                password = BCrypt.hashpw(password, BCrypt.gensalt());
            } else if (password != null && !password.isEmpty()) {
                // Déjà hashé par l'interface.
            }
            statement.setString(4, password);
            statement.setString(5, entity.getRole());
            statement.setBoolean(6, entity.isActif());

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
            System.err.println("[UtilisateurDAO] Error inserting utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Utilisateur entity) {
        String sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, username = ?, "
                + "role = ?, actif = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getPrenom());
            statement.setString(3, entity.getUsername());
            statement.setString(4, entity.getRole());
            statement.setBoolean(5, entity.isActif());
            statement.setInt(6, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error updating utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour un utilisateur en incluant le mot de passe.
     */
    public boolean updateWithPassword(Utilisateur entity) {
        String sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, username = ?, "
                + "password = ?, role = ?, actif = ? WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getNom());
            statement.setString(2, entity.getPrenom());
            statement.setString(3, entity.getUsername());
            String password = entity.getPassword();
            if (password != null && !password.isEmpty() && !password.startsWith("$2")) {
                password = BCrypt.hashpw(password, BCrypt.gensalt());
            }
            statement.setString(4, password);
            statement.setString(5, entity.getRole());
            statement.setBoolean(6, entity.isActif());
            statement.setInt(7, entity.getId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error updating utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error deleting utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Utilisateur findByUsername(String username) {
        String sql = "SELECT * FROM utilisateurs WHERE username = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUtilisateur(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error finding utilisateur by username: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de vérifier les identifiants auprès de la base.", e);
        }
        return null;
    }

    /**
     * Authentifie un utilisateur et retourne l'objet Utilisateur si réussi, null sinon.
     */
    public Utilisateur authenticate(String username, String password) {
        Utilisateur utilisateur = findByUsername(username);
        if (utilisateur == null || password == null) {
            return null;
        }
        String hashedPassword = utilisateur.getPassword();
        try {
            if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
                return utilisateur;
            }
        } catch (IllegalArgumentException invalidHash) {
            System.err.println("[UtilisateurDAO] Hash de mot de passe invalide pour un compte.");
        }
        return null;
    }

    public List<Utilisateur> findByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE role = ? ORDER BY nom ASC";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    utilisateurs.add(mapResultSetToUtilisateur(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Error finding utilisateurs by role: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Impossible de filtrer les utilisateurs dans la base.", e);
        }
        return utilisateurs;
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet resultSet) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(resultSet.getInt("id"));
        utilisateur.setNom(resultSet.getString("nom"));
        utilisateur.setPrenom(resultSet.getString("prenom"));
        utilisateur.setUsername(resultSet.getString("username"));
        utilisateur.setPassword(resultSet.getString("password"));
        utilisateur.setRole(resultSet.getString("role"));
        utilisateur.setActif(resultSet.getBoolean("actif"));
        Timestamp ts = resultSet.getTimestamp("date_creation");
        if (ts != null) {
            utilisateur.setDateCreation(ts.toLocalDateTime());
        }
        return utilisateur;
    }
}
