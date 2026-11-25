package com.pharmacie.views;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.pharmacie.config.AppConstants;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private Utilisateur utilisateur;
    private JPanel contentPanel;
    private JLabel lblUser;
    private JLabel lblDate;

    // Menu items
    private JMenuItem mnuDashboard;
    private JMenuItem mnuMedicaments;
    private JMenuItem mnuCategories;
    private JMenuItem mnuClients;
    private JMenuItem mnuFournisseurs;
    private JMenuItem mnuVentes;
    private JMenuItem mnuApprovisionnements;
    private JMenuItem mnuRapports;
    private JMenuItem mnuAdministration;
    private JButton mnuDeconnexion;
    private JButton mnuQuitter;
    private JMenuItem mnuAPropos;
    private JMenuItem mnuModeJour;
    private JMenuItem mnuModeNuit;

    public MainFrame(Utilisateur utilisateur) {
        super(AppConstants.APP_NAME);
        this.utilisateur = utilisateur;
        initComponents();
        setupMenuBar();
        setupLayout();
        setupListeners();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Afficher le dashboard par défaut
        showDashboard();
    }

    private void initComponents() {
        contentPanel = new JPanel(new BorderLayout());
        lblUser = new JLabel();
        lblDate = new JLabel();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Accueil
        JMenu menuAccueil = new JMenu("Accueil");
        mnuDashboard = new JMenuItem("Tableau de bord");
        menuAccueil.add(mnuDashboard);
        menuBar.add(menuAccueil);

        // Menu Gestion
        JMenu menuGestion = new JMenu("Gestion");
        mnuMedicaments = new JMenuItem("Médicaments");
        mnuCategories = new JMenuItem("Catégories");
        menuGestion.add(mnuMedicaments);
        menuGestion.add(mnuCategories);
        menuGestion.addSeparator();
        mnuClients = new JMenuItem("Clients");
        mnuFournisseurs = new JMenuItem("Fournisseurs");
        menuGestion.add(mnuClients);
        menuGestion.add(mnuFournisseurs);
        menuBar.add(menuGestion);

        // Menu Opérations
        JMenu menuOperations = new JMenu("Opérations");
        mnuVentes = new JMenuItem("Point de vente");
        mnuApprovisionnements = new JMenuItem("Approvisionnement");
        menuOperations.add(mnuVentes);
        menuOperations.add(mnuApprovisionnements);
        menuBar.add(menuOperations);

        // Menu Rapports
        JMenu menuRapports = new JMenu("Rapports");
        mnuRapports = new JMenuItem("Voir les rapports");
        menuRapports.add(mnuRapports);
        menuBar.add(menuRapports);

        // Menu Administration (admin seulement)
        JMenu menuAdmin = new JMenu("Administration");
        mnuAdministration = new JMenuItem("Espace d'administration");
        menuAdmin.add(mnuAdministration);
        menuAdmin.setVisible(utilisateur.isAdmin());

        menuBar.add(menuAdmin);

        // Menu Aide
        JMenu menuAide = new JMenu("Aide");
        mnuAPropos = new JMenuItem("À propos");
        menuAide.add(mnuAPropos);
        menuBar.add(menuAide);

        JMenu menuAffichage = new JMenu("Affichage");
        mnuModeJour = new JMenuItem("Mode jour");
        mnuModeNuit = new JMenuItem("Mode nuit");
        menuAffichage.add(mnuModeJour);
        menuAffichage.add(mnuModeNuit);
        menuBar.add(menuAffichage);

        setJMenuBar(menuBar);
    }

    private void setupLayout() {
        // Barre d'état en bas
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        lblUser.setText("Utilisateur : " + utilisateur.getNomComplet() + " | Rôle : " + utilisateur.getRole());
        lblUser.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
        statusBar.add(lblUser, BorderLayout.WEST);

        mnuDeconnexion = new JButton("Déconnexion");
        mnuQuitter = new JButton("Quitter");
        JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusRight.add(lblDate);
        statusRight.add(mnuDeconnexion);
        statusRight.add(mnuQuitter);
        statusBar.add(statusRight, BorderLayout.EAST);

        // Timer pour mettre à jour la date/heure
        Timer timer = new Timer(1000, e -> {
            lblDate.setText(new java.util.Date().toString());
        });
        timer.start();

        // Content area
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        mnuDashboard.addActionListener(this::onMenuClick);
        mnuMedicaments.addActionListener(this::onMenuClick);
        mnuCategories.addActionListener(this::onMenuClick);
        mnuClients.addActionListener(this::onMenuClick);
        mnuFournisseurs.addActionListener(this::onMenuClick);
        mnuVentes.addActionListener(this::onMenuClick);
        mnuApprovisionnements.addActionListener(this::onMenuClick);
        mnuRapports.addActionListener(this::onMenuClick);
        mnuAdministration.addActionListener(this::onMenuClick);
        mnuModeJour.addActionListener(e -> changeTheme(false));
        mnuModeNuit.addActionListener(e -> changeTheme(true));
        mnuDeconnexion.addActionListener(e -> handleDeconnexion());
        mnuQuitter.addActionListener(e -> handleQuitter());
        mnuAPropos.addActionListener(e -> handleAPropos());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuitter();
            }
        });
    }

    private void changeTheme(boolean dark) {
        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            DialogUtils.showError("Impossible de changer le thème : " + e.getMessage());
        }
    }

    private void onMenuClick(ActionEvent e) {
        Object source = e.getSource();
        if (source == mnuDashboard) showDashboard();
        else if (source == mnuMedicaments) showMedicaments();
        else if (source == mnuCategories) showCategories();
        else if (source == mnuClients) showClients();
        else if (source == mnuFournisseurs) showFournisseurs();
        else if (source == mnuVentes) showVentes();
        else if (source == mnuApprovisionnements) showApprovisionnements();
        else if (source == mnuRapports) showRapports();
        else if (source == mnuAdministration) showAdministration();
    }

    public void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showDashboard() {
        setContentPanel(new DashboardView(this));
    }

    private void showMedicaments() {
        setContentPanel(new MedicamentView(this));
    }

    private void showCategories() {
        setContentPanel(new CategorieView(this));
    }

    private void showClients() {
        setContentPanel(new ClientView(this));
    }

    private void showFournisseurs() {
        setContentPanel(new FournisseurView(this));
    }

    private void showVentes() {
        setContentPanel(new VenteView(this, utilisateur));
    }

    private void showApprovisionnements() {
        setContentPanel(new ApprovisionnementView(this, utilisateur));
    }

    private void showRapports() {
        setContentPanel(new RapportView(this));
    }

    private void showAdministration() {
        if (utilisateur.isAdmin()) {
            setContentPanel(new AdministrationView(this));
        }
    }

    private void handleDeconnexion() {
        if (DialogUtils.showConfirmation("Voulez-vous vous déconnecter ?")) {
            dispose();
            new com.pharmacie.controllers.LoginController().showLoginView();
        }
    }

    private void handleQuitter() {
        if (DialogUtils.showConfirmation("Voulez-vous vraiment quitter l'application ?")) {
            System.exit(0);
        }
    }

    private void handleAPropos() {
        String message = String.format(
                "%s v%s\n\n"
                + "Application de gestion de pharmacie\n"
                + "Développée en Java avec Swing + FlatLaf\n"
                + "Base de données : PostgreSQL / Supabase\n\n"
                + "© 2024 PharmaGestion",
                AppConstants.APP_NAME, AppConstants.APP_VERSION
        );
        JOptionPane.showMessageDialog(this, message,
                "À propos", JOptionPane.INFORMATION_MESSAGE);
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}
