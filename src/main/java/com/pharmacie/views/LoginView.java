package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.pharmacie.config.AppConstants;

public class LoginView extends JDialog {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private PasswordFieldPanel passwordPanel;
    private JButton btnConnexion;
    private JButton btnAnnuler;
    private JLabel lblStatus;

    public LoginView(Frame parent) {
        super(parent, AppConstants.APP_NAME + " - Connexion", true);
        initComponents();
        setupLayout();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtUsername = new JTextField(40);
        txtPassword = new JPasswordField(40);
        passwordPanel = new PasswordFieldPanel(txtPassword);
        btnConnexion = new JButton("Se connecter");
        btnAnnuler = new JButton("Annuler");
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
    }

    private void setupLayout() {
        // Panel principal avec bordure
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(75, 75, 80), 1),
            BorderFactory.createEmptyBorder(28, 38, 28, 38)));
        mainPanel.setBackground(new Color(45, 45, 48));

        // Titre
        JLabel lblTitle = new JLabel(AppConstants.APP_NAME, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(0, 173, 181));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel lblSubtitle = new JLabel("Système de Gestion de Pharmacie", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(45, 45, 48));
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 6, 9, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Nom d'utilisateur :"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(createLabel("Mot de passe :"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        formPanel.add(passwordPanel, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 0;
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblStatus, gbc);

        // Boutons
        gbc.gridy = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(45, 45, 48));
        buttonPanel.add(btnConnexion);
        buttonPanel.add(btnAnnuler);
        formPanel.add(buttonPanel, gbc);

        // Style des composants
        styleFormComponents();

        // Assemblage
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 48));
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        getContentPane().add(mainPanel);
        setSize(520, 380);
        setResizable(false);
        getRootPane().setDefaultButton(btnConnexion);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(155, 36));
        return label;
    }

    private void styleFormComponents() {
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        txtUsername.setFont(fieldFont);
        txtPassword.setFont(fieldFont);
        txtUsername.setForeground(new Color(25, 25, 25));
        txtPassword.setForeground(new Color(25, 25, 25));
        txtUsername.setBackground(Color.WHITE);
        txtPassword.setBackground(Color.WHITE);
        Dimension fieldSize = new Dimension(210, 36);
        txtUsername.setPreferredSize(fieldSize);
        txtPassword.setPreferredSize(fieldSize);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 173, 181), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 173, 181), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnConnexion.setFont(btnFont);
        btnAnnuler.setFont(btnFont);

        btnConnexion.setBackground(new Color(0, 173, 181));
        btnConnexion.setForeground(Color.WHITE);
        btnConnexion.setFocusPainted(false);
        btnConnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAnnuler.setFocusPainted(false);
        btnAnnuler.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Dimension buttonSize = new Dimension(140, 38);
        btnConnexion.setPreferredSize(buttonSize);
        btnAnnuler.setPreferredSize(buttonSize);
        btnAnnuler.setBackground(new Color(238, 238, 238));
        btnAnnuler.setForeground(new Color(35, 35, 35));
    }

    // Getters
    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public char[] getPassword() {
        return txtPassword.getPassword();
    }

    public JButton getBtnConnexion() {
        return btnConnexion;
    }

    public JButton getBtnAnnuler() {
        return btnAnnuler;
    }

    public void setStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.setForeground(error ? Color.RED : new Color(76, 175, 80));
    }

    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocusInWindow();
    }

    public void setLoginInProgress(boolean inProgress) {
        btnConnexion.setEnabled(!inProgress);
        btnAnnuler.setEnabled(!inProgress);
        txtUsername.setEnabled(!inProgress);
        passwordPanel.setPasswordEnabled(!inProgress);
    }
}
