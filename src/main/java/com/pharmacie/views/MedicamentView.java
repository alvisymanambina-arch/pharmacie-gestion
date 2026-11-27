package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.pharmacie.dao.CategorieDAO;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.models.Categorie;
import com.pharmacie.models.Medicament;
import com.pharmacie.utils.DateUtils;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.FormatUtils;
import com.pharmacie.utils.TableUi;
import com.pharmacie.utils.FormValidation;

public class MedicamentView extends JPanel {

    private final MedicamentDAO medicamentDAO;
    private final CategorieDAO categorieDAO;

    private JTextField txtRecherche;
    private JComboBox<Categorie> cmbCategorie;
    private JTable tblMedicaments;
    private DefaultTableModel model;
    private final AtomicLong tableRequest = new AtomicLong();
    private boolean loadingCategories;

    public MedicamentView(MainFrame mainFrame) {
        this.medicamentDAO = new MedicamentDAO();
        this.categorieDAO = new CategorieDAO();

        initComponents();
        setupLayout();
        loadCategories();
        loadData();
    }

    private void initComponents() {
        txtRecherche = new JTextField(20);
        cmbCategorie = new JComboBox<>();

        model = new DefaultTableModel(
            new Object[]{"#", "Nom", "Catégorie", "Dosage", "Forme", "Prix Achat", "Prix Vente",
                "Stock", "Seuil", "Expiration", "Code Barre", "ID"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMedicaments = new JTable(model);
        TableUi.apply(tblMedicaments);
        tblMedicaments.setAutoCreateRowSorter(true);
        tblMedicaments.getColumnModel().getColumn(0).setMaxWidth(50);
        tblMedicaments.getColumnModel().getColumn(11).setMinWidth(0);
        tblMedicaments.getColumnModel().getColumn(11).setMaxWidth(0);
        tblMedicaments.getColumnModel().getColumn(11).setPreferredWidth(0);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titre
        JLabel lblTitle = new JLabel("Gestion des Médicaments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // Barre de recherche
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.add(new JLabel("Rechercher :"));
        panelSearch.add(txtRecherche);
        JButton btnRechercher = new JButton("Rechercher");
        panelSearch.add(btnRechercher);
        panelSearch.add(Box.createHorizontalStrut(20));
        panelSearch.add(new JLabel("Catégorie :"));
        cmbCategorie.setPreferredSize(new Dimension(180, 28));
        panelSearch.add(cmbCategorie);
        JButton btnTous = new JButton("Afficher tout");
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

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(lblTitle, BorderLayout.NORTH);
        JPanel panelFilters = new JPanel(new BorderLayout());
        panelFilters.add(panelSearch, BorderLayout.NORTH);
        panelFilters.add(panelBoutons, BorderLayout.SOUTH);
        panelTop.add(panelFilters, BorderLayout.CENTER);

        // Listeners
        btnRechercher.addActionListener(e -> rechercher());
        txtRecherche.addActionListener(e -> rechercher());
        btnTous.addActionListener(e -> {
            cmbCategorie.setSelectedIndex(0);
            loadData();
        });
        cmbCategorie.addActionListener(e -> {
            if (!loadingCategories) filtrerParCategorie();
        });
        btnAjouter.addActionListener(e -> ouvrirDialogAjout());
        btnModifier.addActionListener(e -> ouvrirDialogModification());
        btnSupprimer.addActionListener(e -> supprimer());
        btnActualiser.addActionListener(e -> loadData());

        add(panelTop, BorderLayout.NORTH);
        add(new JScrollPane(tblMedicaments), BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadCategories() {
        loadingCategories = true;
        cmbCategorie.setEnabled(false);
        new SwingWorker<List<Categorie>, Void>() {
            @Override
            protected List<Categorie> doInBackground() {
                return categorieDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    cmbCategorie.removeAllItems();
                    cmbCategorie.addItem(new Categorie(0, "Toutes les catégories", null));
                    for (Categorie categorie : get()) cmbCategorie.addItem(categorie);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement des catégories");
                } finally {
                    loadingCategories = false;
                    cmbCategorie.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadData() {
        long requestId = tableRequest.incrementAndGet();
        SwingWorker<List<Medicament>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Medicament> doInBackground() {
                return medicamentDAO.findAll();
            }

            @Override
            protected void done() {
                if (requestId != tableRequest.get()) return;
                try {
                    afficherMedicaments(get());
                } catch (InterruptedException| ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement");
                }
            }
        };
        worker.execute();
    }

    private void rechercher() {
        String keyword = txtRecherche.getText().trim();
        long requestId = tableRequest.incrementAndGet();
        SwingWorker<List<Medicament>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Medicament> doInBackground() {
                return medicamentDAO.searchByNom(keyword);
            }

            @Override
            protected void done() {
                if (requestId != tableRequest.get()) return;
                try {
                    afficherMedicaments(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de recherche");
                }
            }
        };
        worker.execute();
    }

    private void filtrerParCategorie() {
        Categorie selected = (Categorie) cmbCategorie.getSelectedItem();
        if (selected == null || selected.getId() == 0) {
            loadData();
            return;
        }
        long requestId = tableRequest.incrementAndGet();
        SwingWorker<List<Medicament>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Medicament> doInBackground() {
                return medicamentDAO.findByCategorie(selected.getId());
            }

            @Override
            protected void done() {
                if (requestId != tableRequest.get()) return;
                try {
                    afficherMedicaments(get());
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de filtrage");
                }
            }
        };
        worker.execute();
    }

    private void afficherMedicaments(List<Medicament> medicaments) {
        model.setRowCount(0);
        for (int index = 0; index < medicaments.size(); index++) {
            Medicament m = medicaments.get(index);
            model.addRow(new Object[]{
                index + 1,
                    m.getNom(),
                    m.getCategorieNom() != null ? m.getCategorieNom() : "",
                    m.getDosage() != null ? m.getDosage() : "",
                    m.getForme() != null ? m.getForme() : "",
                    FormatUtils.formatCurrency(m.getPrixAchat()),
                    FormatUtils.formatCurrency(m.getPrixVente()),
                    m.getQuantiteStock(),
                    m.getSeuilAlerte(),
                    DateUtils.formatDate(m.getDateExpiration()),
                    m.getCodeBarre() != null ? m.getCodeBarre() : "",
                    m.getId()
            });
        }
    }

    private void ouvrirDialogAjout() {
        MedicamentDialog dialog = new MedicamentDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Ajouter un médicament", null, categorieDAO);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadData();
        }
    }

    private void ouvrirDialogModification() {
        int row = tblMedicaments.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un médicament à modifier.");
            return;
        }
        row = tblMedicaments.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 11);
        new SwingWorker<Medicament, Void>() {
            @Override
            protected Medicament doInBackground() {
                return medicamentDAO.findById(id);
            }

            @Override
            protected void done() {
                try {
                    Medicament medicament = get();
                    if (medicament == null) {
                        DialogUtils.showError("Médicament introuvable.");
                        return;
                    }
                    MedicamentDialog dialog = new MedicamentDialog(
                            (Frame) SwingUtilities.getWindowAncestor(MedicamentView.this),
                            "Modifier le médicament", medicament, categorieDAO);
                    dialog.setVisible(true);
                    if (dialog.isConfirmed()) loadData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement du médicament");
                }
            }
        }.execute();
    }

    private void supprimer() {
        int row = tblMedicaments.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un médicament à supprimer.");
            return;
        }
        row = tblMedicaments.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 11);
        String nom = (String) model.getValueAt(row, 1);
        if (DialogUtils.showConfirmation(
                "Voulez-vous vraiment supprimer le médicament :\n" + nom + " ?")) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return medicamentDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            DialogUtils.showInformation("Médicament supprimé avec succès.");
                            loadData();
                        } else {
                            DialogUtils.showError(
                                "Impossible de supprimer : ce médicament est référencé "
                                + "par des ventes ou approvisionnements.");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur lors de la suppression");
                    }
                }
            };
            worker.execute();
        }
    }

    // ============================================
    // Dialog d'ajout / modification
    // ============================================
    private static class MedicamentDialog extends JDialog {

        private final MedicamentDAO medicamentDAO = new MedicamentDAO();
        private final CategorieDAO categorieDAO;
        private boolean confirmed = false;

        private int editingId = 0;
        private int initialCategoryId;

        private JTextField txtNom;
        private JTextField txtDosage;
        private JTextField txtForme;
        private JTextArea txtDescription;
        private JComboBox<Categorie> cmbCategorie;
        private JTextField txtPrixAchat;
        private JTextField txtPrixVente;
        private JTextField txtStock;
        private JTextField txtSeuil;
        private JFormattedTextField txtExpiration;
        private JTextField txtCodeBarre;
        private JButton btnOK;

        public MedicamentDialog(Frame parent, String title, Medicament med, CategorieDAO catDAO) {
            super(parent, title, true);
            this.categorieDAO = catDAO;
            initComponents();
            setupLayout();
            loadCategories();
            if (med != null) fillFields(med);
            setSize(480, 520);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            txtNom = new JTextField(25);
            txtDosage = new JTextField(15);
            txtForme = new JTextField(15);
            txtDescription = new JTextArea(3, 25);
            cmbCategorie = new JComboBox<>();
            txtPrixAchat = new JTextField(10);
            txtPrixVente = new JTextField(10);
            txtStock = new JTextField(10);
            txtStock.setEditable(false);
            txtStock.setText("0");
            txtSeuil = new JTextField(10);
            txtCodeBarre = new JTextField(20);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            txtExpiration = new JFormattedTextField(sdf);
            txtExpiration.setColumns(10);
            txtExpiration.setEditable(false);
        }

        private void setupLayout() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            addRow(panel, gbc, "Nom * :", txtNom, y++);
            addRow(panel, gbc, "Dosage :", txtDosage, y++);
            addRow(panel, gbc, "Forme :", txtForme, y++);
            addRow(panel, gbc, "Description :", new JScrollPane(txtDescription), y++);
            addRow(panel, gbc, "Catégorie * :", cmbCategorie, y++);
            addRow(panel, gbc, "Prix d'achat (Ar) * :", txtPrixAchat, y++);
            addRow(panel, gbc, "Prix de vente (Ar) * :", txtPrixVente, y++);
            addRow(panel, gbc, "Stock (géré par approvisionnement) :", txtStock, y++);
            addRow(panel, gbc, "Seuil d'alerte :", txtSeuil, y++);
            addRow(panel, gbc, "Prochaine expiration (par lot) :", txtExpiration, y++);
            addRow(panel, gbc, "Code-barres :", txtCodeBarre, y++);

            // Boutons
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

        private void loadCategories() {
            cmbCategorie.setEnabled(false);
            btnOK.setEnabled(false);
            new SwingWorker<List<Categorie>, Void>() {
                @Override
                protected List<Categorie> doInBackground() {
                    return categorieDAO.findAll();
                }

                @Override
                protected void done() {
                    try {
                        cmbCategorie.removeAllItems();
                        for (Categorie categorie : get()) cmbCategorie.addItem(categorie);
                        for (int i = 0; i < cmbCategorie.getItemCount(); i++) {
                            if (cmbCategorie.getItemAt(i).getId() == initialCategoryId) {
                                cmbCategorie.setSelectedIndex(i);
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur de chargement des catégories");
                    } finally {
                        cmbCategorie.setEnabled(true);
                        btnOK.setEnabled(true);
                    }
                }
            }.execute();
        }

        private void fillFields(Medicament m) {
            this.editingId = m.getId();
            this.initialCategoryId = m.getCategorieId();
            txtNom.setText(m.getNom());
            txtDosage.setText(m.getDosage());
            txtForme.setText(m.getForme());
            txtDescription.setText(m.getDescription());
            txtPrixAchat.setText(m.getPrixAchat().toString());
            txtPrixVente.setText(m.getPrixVente().toString());
            txtStock.setText(String.valueOf(m.getQuantiteStock()));
            txtSeuil.setText(String.valueOf(m.getSeuilAlerte()));
            if (m.getDateExpiration() != null) {
                txtExpiration.setValue(java.sql.Date.valueOf(m.getDateExpiration()));
            }
            txtCodeBarre.setText(m.getCodeBarre());
        }

        private void onOK() {
            if (!validateFields()) return;

            Medicament m = new Medicament();
            m.setNom(txtNom.getText().trim());
            m.setDosage(txtDosage.getText().trim());
            m.setForme(txtForme.getText().trim());
            m.setDescription(txtDescription.getText().trim());
            Categorie cat = (Categorie) cmbCategorie.getSelectedItem();
            m.setCategorieId(cat != null ? cat.getId() : 0);
            m.setPrixAchat(parsePriceField(txtPrixAchat.getText()));
            m.setPrixVente(parsePriceField(txtPrixVente.getText()));
            m.setSeuilAlerte(FormatUtils.parseInteger(txtSeuil.getText()));
            m.setCodeBarre(txtCodeBarre.getText().trim().isEmpty() ? null
                    : txtCodeBarre.getText().trim());

            if (editingId > 0) {
                m.setId(editingId);
            }
            btnOK.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return editingId > 0 ? medicamentDAO.update(m) : medicamentDAO.insert(m);
                }

                @Override
                protected void done() {
                    btnOK.setEnabled(true);
                    try {
                        if (get()) {
                            confirmed = true;
                            dispose();
                        } else {
                            DialogUtils.showError("Erreur lors de l'enregistrement.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        DialogUtils.showError("Enregistrement interrompu.");
                    } catch (ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur lors de l'enregistrement");
                    }
                }
            }.execute();
        }

        private boolean validateFields() {
            FormValidation.clear(txtNom, txtDosage, txtForme, cmbCategorie,
                    txtPrixAchat, txtPrixVente, txtSeuil, txtCodeBarre);
            if (txtNom.getText().trim().isEmpty()) {
                FormValidation.invalid(txtNom, "Le nom est obligatoire.");
                return false;
            }
            if (txtNom.getText().trim().length() > 200) {
                FormValidation.invalid(txtNom, "Le nom ne peut pas dépasser 200 caractères.");
                return false;
            }
            if (txtDosage.getText().trim().length() > 100) {
                FormValidation.invalid(txtDosage, "Le dosage ne peut pas dépasser 100 caractères.");
                return false;
            }
            if (txtForme.getText().trim().length() > 100) {
                FormValidation.invalid(txtForme, "La forme ne peut pas dépasser 100 caractères.");
                return false;
            }
            Categorie categorie = (Categorie) cmbCategorie.getSelectedItem();
            if (categorie == null || categorie.getId() <= 0) {
                FormValidation.invalid(cmbCategorie, "Veuillez sélectionner une catégorie valide.");
                return false;
            }
            try {
                if (parsePriceField(txtPrixAchat.getText()).signum() < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                FormValidation.invalid(txtPrixAchat, "Le prix d'achat doit être un montant nul ou positif (maximum 8 chiffres avant la virgule).");
                return false;
            }
            try {
                if (parsePriceField(txtPrixVente.getText()).signum() <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                FormValidation.invalid(txtPrixVente, "Le prix de vente doit être un montant positif (maximum 8 chiffres avant la virgule).");
                return false;
            }
            String seuil = txtSeuil.getText().trim();
            try {
                if (!seuil.matches("\\d+") || Integer.parseInt(seuil) < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                FormValidation.invalid(txtSeuil, "Le seuil doit être un entier positif ou nul.");
                return false;
            }
            if (txtCodeBarre.getText().trim().length() > 50) {
                FormValidation.invalid(txtCodeBarre, "Le code-barres ne peut pas dépasser 50 caractères.");
                return false;
            }
            return true;
        }

        private BigDecimal parsePriceField(String value) {
            String normalized = value == null ? "" : value.trim()
                    .replace("\u00A0", "")
                    .replace(" ", "")
                    .replace(',', '.');
            if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
                throw new NumberFormatException("Montant invalide");
            }
            BigDecimal amount = new BigDecimal(normalized);
            if (amount.precision() - amount.scale() > 8) {
                throw new NumberFormatException("Montant trop élevé");
            }
            return amount.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        public boolean isConfirmed() {
            return confirmed;
        }
    }
}
