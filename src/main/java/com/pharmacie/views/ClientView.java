package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.models.Client;
import com.pharmacie.utils.DialogUtils;

public class ClientView extends JPanel {

  
    private final ClientDAO clientDAO;

    private JTextField txtRecherche;
    private JTable tblClients;
    private DefaultTableModel model;

    public ClientView(MainFrame mainFrame) {
       
        this.clientDAO = new ClientDAO();

        initComponents();
        setupLayout();
        loadTable();
    }

    private void initComponents() {
        txtRecherche = new JTextField(20);

        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Prénom", "Adresse", "Téléphone", "Email"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblClients = new JTable(model);
        tblClients.setRowHeight(26);
        tblClients.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Gestion des Clients");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // Recherche
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.add(new JLabel("Rechercher :"));
        panelSearch.add(txtRecherche);
        JButton btnRechercher = new JButton("Rechercher");
        JButton btnTous = new JButton("Afficher tout");
        panelSearch.add(btnRechercher);
        panelSearch.add(btnTous);

        // Boutons CRUD
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAjouter = new JButton("➕ Ajouter");
        JButton btnModifier = new JButton("✏️ Modifier");
        JButton btnSupprimer = new JButton("🗑️ Supprimer");
        JButton btnActualiser = new JButton("🔄 Actualiser");
        styleButton(btnAjouter, new Color(76, 175, 80));
        styleButton(btnModifier, new Color(33, 150, 243));
        styleButton(btnSupprimer, new Color(244, 67, 54));
        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnActualiser);

        btnRechercher.addActionListener(e -> rechercher());
        txtRecherche.addActionListener(e -> rechercher());
        btnTous.addActionListener(e -> loadTable());
        btnAjouter.addActionListener(e -> ouvrirDialog(null));
        btnModifier.addActionListener(e -> modifier());
        btnSupprimer.addActionListener(e -> supprimer());
        btnActualiser.addActionListener(e -> loadTable());

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(lblTitle, BorderLayout.NORTH);
        JPanel panelFilters = new JPanel(new BorderLayout());
        panelFilters.add(panelSearch, BorderLayout.NORTH);
        panelFilters.add(panelBoutons, BorderLayout.SOUTH);
        panelTop.add(panelFilters, BorderLayout.CENTER);

        add(panelTop, BorderLayout.NORTH);
        add(new JScrollPane(tblClients), BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadTable() {
        SwingWorker<List<Client>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Client> doInBackground() {
                return clientDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    afficherClients(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }

    private void rechercher() {
        String keyword = txtRecherche.getText().trim();
        SwingWorker<List<Client>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Client> doInBackground() {
                return clientDAO.searchByNom(keyword);
            }

            @Override
            protected void done() {
                try {
                    afficherClients(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }

    private void afficherClients(List<Client> clients) {
        model.setRowCount(0);
        for (Client c : clients) {
            model.addRow(new Object[]{
                    c.getId(), c.getNom(), c.getPrenom(),
                    c.getAdresse() != null ? c.getAdresse() : "",
                    c.getTelephone() != null ? c.getTelephone() : "",
                    c.getEmail() != null ? c.getEmail() : ""
            });
        }
    }

    private void modifier() {
        int row = tblClients.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un client.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Client c = clientDAO.findById(id);
        if (c != null) {
            ouvrirDialog(c);
        }
    }

    private void supprimer() {
        int row = tblClients.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un client.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String nom = model.getValueAt(row, 1) + " " + model.getValueAt(row, 2);
        if (DialogUtils.showConfirmation(
                "Voulez-vous vraiment supprimer le client :\n" + nom + " ?")) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return clientDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            DialogUtils.showInformation("Client supprimé.");
                            loadTable();
                        } else {
                            DialogUtils.showError(
                                "Impossible de supprimer ce client.");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur");
                    }
                }
            };
            worker.execute();
        }
    }

    private void ouvrirDialog(Client client) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String title = client == null ? "Ajouter un client" : "Modifier le client";
        ClientDialog dialog = new ClientDialog(parent, title, client);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTable();
        }
    }

    private class ClientDialog extends JDialog {

        private boolean confirmed = false;
        private int editingId = 0;
        private JTextField txtNom, txtPrenom, txtTelephone, txtEmail;
        private JTextArea txtAdresse;

        public ClientDialog(Frame parent, String title, Client client) {
            super(parent, title, true);
            initComponents();
            if (client != null) {
                editingId = client.getId();
                txtNom.setText(client.getNom());
                txtPrenom.setText(client.getPrenom());
                txtAdresse.setText(client.getAdresse());
                txtTelephone.setText(client.getTelephone());
                txtEmail.setText(client.getEmail());
            }
            setSize(420, 350);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            txtNom = new JTextField(25);
            txtPrenom = new JTextField(25);
            txtTelephone = new JTextField(25);
            txtEmail = new JTextField(25);
            txtAdresse = new JTextArea(3, 25);

            int y = 0;
            addRow(panel, gbc, "Nom * :", txtNom, y++);
            addRow(panel, gbc, "Prénom * :", txtPrenom, y++);
            addRow(panel, gbc, "Adresse :", new JScrollPane(txtAdresse), y++);
            addRow(panel, gbc, "Téléphone :", txtTelephone, y++);
            addRow(panel, gbc, "Email :", txtEmail, y++);

            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            JButton btnOK = new JButton("Enregistrer");
            btnOK.setBackground(new Color(76, 175, 80));
            btnOK.setForeground(Color.WHITE);
            JButton btnCancel = new JButton("Annuler");
            btnOK.addActionListener(e -> onOK());
            btnCancel.addActionListener(e -> dispose());
            panelBtn.add(btnOK);
            panelBtn.add(btnCancel);
            panel.add(panelBtn, gbc);

            add(panel);
        }

        private void addRow(JPanel panel, GridBagConstraints gbc,
                            String label, JComponent field, int y) {
            gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
            panel.add(new JLabel(label), gbc);
            gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1.0;
            panel.add(field, gbc);
        }

        private void onOK() {
            if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()) {
                DialogUtils.showWarning("Le nom et le prénom sont obligatoires.");
                return;
            }
            Client c = new Client();
            c.setNom(txtNom.getText().trim());
            c.setPrenom(txtPrenom.getText().trim());
            c.setAdresse(txtAdresse.getText().trim());
            c.setTelephone(txtTelephone.getText().trim());
            c.setEmail(txtEmail.getText().trim());

            boolean success;
            if (editingId > 0) {
                c.setId(editingId);
                success = clientDAO.update(c);
            } else {
                success = clientDAO.insert(c);
            }

            if (success) {
                confirmed = true;
                dispose();
            } else {
                DialogUtils.showError("Erreur lors de l'enregistrement.");
            }
        }

        public boolean isConfirmed() { return confirmed; }
    }
}
