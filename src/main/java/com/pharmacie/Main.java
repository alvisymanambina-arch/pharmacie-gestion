package com.pharmacie;

import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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

        SwingUtilities.invokeLater(Main::connectAndLaunch);
    }

    private static void connectAndLaunch() {
        JFrame loading = new JFrame(AppConstants.APP_NAME);
        loading.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        content.add(new JLabel("Connexion à la base de données…", JLabel.CENTER), BorderLayout.CENTER);
        loading.setContentPane(content);
        loading.setSize(380, 110);
        loading.setLocationRelativeTo(null);
        loading.setVisible(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return DatabaseConfig.getInstance().testConnection();
            }

            @Override
            protected void done() {
                loading.dispose();
                try {
                    if (get()) new LoginController().showLoginView();
                    else showConnectionError();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showConnectionError();
                } catch (ExecutionException e) {
                    showConnectionError();
                }
            }
        }.execute();
    }

    private static void showConnectionError() {
        DialogUtils.showError(
                """
                Impossible de se connecter à la base de données.
                Vérifiez l'accès internet, les paramètres .env et le schéma Supabase.

                URL : """ + AppConstants.DB_URL + "\n"
                + "Utilisateur : " + AppConstants.DB_USER
        );
    }
}
