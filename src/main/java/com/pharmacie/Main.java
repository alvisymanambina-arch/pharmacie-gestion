package com.pharmacie;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.pharmacie.config.AppConstants;
import com.pharmacie.config.DatabaseConfig;
import com.pharmacie.controllers.LoginController;
import com.pharmacie.utils.DialogUtils;

public class Main {

    public static void main(String[] args) {
        // Configuration du thème FlatLaf
        try {
            FlatDarkLaf.setup();
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du thème : " + e.getMessage());
        }

        // Vérifier la connexion à la base de données
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        if (!dbConfig.testConnection()) {
            DialogUtils.showError(
                    """
                    Impossible de se connecter \u00e0 la base de donn\u00e9es.
                    Veuillez v\u00e9rifier votre connexion PostgreSQL/Supabase et le sch\u00e9ma pharmacie.
                    
                    URL : """ + AppConstants.DB_URL + "\n" +
                    "Utilisateur : " + AppConstants.DB_USER
            );
            System.exit(1);
        }

        // Lancer l'application sur l'EDT
        SwingUtilities.invokeLater(() -> {
            LoginController loginController = new LoginController();
            loginController.showLoginView();
        });
    }
}
