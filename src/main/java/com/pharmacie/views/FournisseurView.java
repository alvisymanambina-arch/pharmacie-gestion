package com.pharmacie.views;

import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.models.Fournisseur;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.TableUi;
import com.pharmacie.utils.FormValidation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class FournisseurView extends JPanel {

    private final FournisseurDAO fournisseurDAO;

    private JTextField txtRecherche;
    private JTable tblFournisseurs;
    private DefaultTableModel model;
    private final AtomicLong tableRequest = new AtomicLong();

    public FournisseurView(MainFrame mainFrame) {
        this.fournisseurDAO = new FournisseurDAO();

        initComponents();
        setupLayout();
        loadTable();
    }

    private void initComponents() {
        txtRecherche = new JTextField(20);

        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Adresse", "Téléphone", "Email", "Contact"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblFournisseurs = new JTable(model);
        TableUi.apply(tblFournisseurs);
        tblFournisseurs.setAutoCreateRowSorter(true);
        tblFournisseurs.setAutoCreateRowSorter(true);
        tblFournisseurs.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Gestion des Fournisseurs");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.add(new JLabel("Rechercher :"));
        panelSearch.add(txtRecherche);
        JButton btnRechercher = new JButton("Rechercher");
        JButton btnTous = new JButton("Afficher tout");
        panelSearch.add(btnRechercher);
        panelSearch.add(btnTous);

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
        add(new JScrollPane(tblFournisseurs), BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadTable() {
        long requestId = tableRequest.incrementAndGet();
        SwingWorker<List<Fournisseur>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Fournisseur> doInBackground() {
                return fournisseurDAO.findAll();
            }

            @Override
            protected void done() {
                if (requestId != tableRequest.get()) return;
                try {
                    afficherFournisseurs(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }

    private void rechercher() {
        String keyword = txtRecherche.getText().trim();
        long requestId = tableRequest.incrementAndGet();
        SwingWorker<List<Fournisseur>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Fournisseur> doInBackground() {
                return fournisseurDAO.searchByNom(keyword);
            }

            @Override
            protected void done() {
                if (requestId != tableRequest.get()) return;
                try {
                    afficherFournisseurs(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }

    private void afficherFournisseurs(List<Fournisseur> fournisseurs) {
        model.setRowCount(0);
        for (Fournisseur f : fournisseurs) {
            model.addRow(new Object[]{
                    f.getId(), f.getNom(),
                    f.getAdresse() != null ? f.getAdresse() : "",
                    f.getTelephone() != null ? f.getTelephone() : "",
                    f.getEmail() != null ? f.getEmail() : "",
                    f.getContactPerson() != null ? f.getContactPerson() : ""
            });
        }
    }

    private void modifier() {
        int row = tblFournisseurs.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un fournisseur.");
            return;
        }
        row = tblFournisseurs.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 0);
        Fournisseur f = new Fournisseur(id,
                String.valueOf(model.getValueAt(row, 1)),
                String.valueOf(model.getValueAt(row, 2)),
                String.valueOf(model.getValueAt(row, 3)),
                String.valueOf(model.getValueAt(row, 4)),
                String.valueOf(model.getValueAt(row, 5)));
        ouvrirDialog(f);
    }

    private void supprimer() {
        int row = tblFournisseurs.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un fournisseur.");
            return;
        }
        row = tblFournisseurs.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);
        if (DialogUtils.showConfirmation(
                "Voulez-vous vraiment supprimer le fournisseur :\n" + nom + " ?")) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return fournisseurDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            DialogUtils.showInformation("Fournisseur supprimé.");
                            loadTable();
                        } else {
                            DialogUtils.showError(
                                "Impossible de supprimer ce fournisseur.");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur");
                    }
                }
            };
            worker.execute();
        }
    }

    private void ouvrirDialog(Fournisseur fournisseur) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String title = fournisseur == null ? "Ajouter un fournisseur" : "Modifier le fournisseur";
        FournisseurDialog dialog = new FournisseurDialog(parent, title, fournisseur);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTable();
        }
    }

    private class FournisseurDialog extends JDialog {

        private boolean confirmed = false;
        private int editingId = 0;
        private JButton btnOK;
        private JTextField txtNom, txtTelephone, txtEmail, txtContact;
        private JTextArea txtAdresse;

        public FournisseurDialog(Frame parent, String title, Fournisseur fournisseur) {
            super(parent, title, true);
            initComponents();
            if (fournisseur != null) {
                editingId = fournisseur.getId();
                txtNom.setText(fournisseur.getNom());
                txtAdresse.setText(fournisseur.getAdresse());
                txtTelephone.setText(fournisseur.getTelephone());
                txtEmail.setText(fournisseur.getEmail());
                txtContact.setText(fournisseur.getContactPerson());
            }
            setSize(420, 380);
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
            txtTelephone = new JTextField(25);
            txtEmail = new JTextField(25);
            txtContact = new JTextField(25);
            txtAdresse = new JTextArea(3, 25);

            int y = 0;
            addRow(panel, gbc, "Nom * :", txtNom, y++);
            addRow(panel, gbc, "Adresse :", new JScrollPane(txtAdresse), y++);
            addRow(panel, gbc, "Téléphone :", txtTelephone, y++);
            addRow(panel, gbc, "Email :", txtEmail, y++);
            addRow(panel, gbc, "Personne contact :", txtContact, y++);

            gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            btnOK = new JButton("Enregistrer");
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
            FormValidation.clear(txtNom, txtTelephone, txtEmail, txtContact);
            if (txtNom.getText().trim().isEmpty()) {
                FormValidation.invalid(txtNom, "Le nom est obligatoire.");
                return;
            }
            if (txtNom.getText().trim().length() > 200) {
                FormValidation.invalid(txtNom, "Le nom ne peut pas dépasser 200 caractères.");
                return;
            }
            if (txtContact.getText().trim().length() > 100) {
                FormValidation.invalid(txtContact, "Le contact ne peut pas dépasser 100 caractères.");
                return;
            }
            if (!FormValidation.isValidPhone(txtTelephone.getText().trim())) {
                FormValidation.invalid(txtTelephone, "Numéro invalide : utilisez 7 à 15 chiffres (maximum 20 caractères).");
                return;
            }
            if (!FormValidation.isValidEmail(txtEmail.getText().trim())) {
                FormValidation.invalid(txtEmail, "Adresse e-mail invalide ou trop longue (100 caractères maximum).");
                return;
            }
            Fournisseur f = new Fournisseur();
            f.setNom(txtNom.getText().trim());
            f.setAdresse(txtAdresse.getText().trim());
            f.setTelephone(txtTelephone.getText().trim());
            f.setEmail(txtEmail.getText().trim());
            f.setContactPerson(txtContact.getText().trim());

            if (editingId > 0) {
                f.setId(editingId);
            }
            btnOK.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return editingId > 0 ? fournisseurDAO.update(f) : fournisseurDAO.insert(f);
                }
                @Override protected void done() {
                    btnOK.setEnabled(true);
                    try {
                        if (get()) { confirmed = true; dispose(); }
                        else DialogUtils.showError("Erreur lors de l'enregistrement.");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        DialogUtils.showError("Enregistrement interrompu.");
                    } catch (ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur lors de l'enregistrement");
                    }
                }
            }.execute();
        }

        public boolean isConfirmed() { return confirmed; }
    }
}
