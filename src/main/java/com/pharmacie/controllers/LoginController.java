package com.pharmacie.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;

import javax.swing.SwingWorker;

import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.views.LoginView;
import com.pharmacie.views.MainFrame;

public class LoginController {

    private LoginView loginView;
    private final UtilisateurDAO utilisateurDAO;

    public LoginController() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public void showLoginView() {
        loginView = new LoginView(null);
        setupListeners();
        loginView.setVisible(true);
    }

    private void setupListeners() {
        loginView.getBtnConnexion().addActionListener((ActionEvent e) -> {
            handleLogin();
        });

        loginView.getBtnAnnuler().addActionListener((ActionEvent e) -> {
            handleAnnuler();
        });

        loginView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void handleLogin() {
        String username = loginView.getUsername();
        char[] password = loginView.getPassword();

        if (username.isEmpty() || password.length == 0) {
            loginView.setStatus("Veuillez remplir tous les champs.", true);
            return;
        }

        loginView.setStatus("Connexion en cours...", false);
        loginView.setLoginInProgress(true);

        // Authentification dans un thread séparé pour ne pas bloquer l'UI
        SwingWorker<Utilisateur, Void> worker = new SwingWorker<>() {
            @Override
            protected Utilisateur doInBackground() {
                try {
                    return utilisateurDAO.authenticate(username, new String(password));
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                loginView.setLoginInProgress(false);
                try {
                    Utilisateur user = get();
                    if (user != null && user.isActif()) {
                        loginView.dispose();
                        openMainFrame(user);
                    } else if (user != null && !user.isActif()) {
                        loginView.setStatus("Compte désactivé. Contactez l'administrateur.", true);
                    } else {
                        loginView.setStatus("Nom d'utilisateur ou mot de passe incorrect.", true);
                        loginView.clearFields();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    loginView.setStatus("Erreur de connexion au serveur.", true);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }
        };
        worker.execute();
    }

    private void handleAnnuler() {
        loginView.dispose();
        System.exit(0);
    }

    private void openMainFrame(Utilisateur utilisateur) {
        MainFrame mainFrame = new MainFrame(utilisateur);
        mainFrame.setVisible(true);
    }
}
