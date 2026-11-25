package com.pharmacie.dao;

import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.models.LotMedicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LotMedicamentDAO {
    private final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();

    public List<LotMedicament> findDisponiblesFefo(int medicamentId) {
        String sql = "SELECT * FROM lots_medicaments WHERE medicament_id = ? AND quantite_disponible > 0 "
                + "AND date_expiration >= CURRENT_DATE ORDER BY date_expiration, id";
        List<LotMedicament> lots = new ArrayList<>();
        try (Connection c = databaseConfig.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, medicamentId);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) lots.add(map(rs)); }
        } catch (SQLException e) { System.err.println("[LotMedicamentDAO] " + e.getMessage()); }
        return lots;
    }

    public boolean insert(LotMedicament lot) {
        String sql = "INSERT INTO lots_medicaments (medicament_id, numero_lot, date_expiration, quantite_initiale, quantite_disponible, prix_achat) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = databaseConfig.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, lot.getMedicamentId()); s.setString(2, lot.getNumeroLot());
            s.setDate(3, Date.valueOf(lot.getDateExpiration())); s.setInt(4, lot.getQuantiteInitiale());
            s.setInt(5, lot.getQuantiteDisponible()); s.setBigDecimal(6, lot.getPrixAchat());
            if (s.executeUpdate() == 0) return false;
            try (ResultSet keys = s.getGeneratedKeys()) { if (keys.next()) lot.setId(keys.getInt(1)); }
            return true;
        } catch (SQLException e) { System.err.println("[LotMedicamentDAO] " + e.getMessage()); return false; }
    }

    private LotMedicament map(ResultSet rs) throws SQLException {
        LotMedicament lot = new LotMedicament(); lot.setId(rs.getInt("id")); lot.setMedicamentId(rs.getInt("medicament_id"));
        lot.setNumeroLot(rs.getString("numero_lot")); lot.setDateExpiration(rs.getDate("date_expiration").toLocalDate());
        lot.setQuantiteInitiale(rs.getInt("quantite_initiale")); lot.setQuantiteDisponible(rs.getInt("quantite_disponible")); lot.setPrixAchat(rs.getBigDecimal("prix_achat")); return lot;
    }
}
