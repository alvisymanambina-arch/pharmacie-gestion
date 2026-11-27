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

import com.pharmacie.dao.CategorieDAO;
import com.pharmacie.models.Categorie;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.TableUi;
import com.pharmacie.utils.FormValidation;

public class CategorieView extends JPanel {


    private final CategorieDAO categorieDAO;

    private JTable tblCategories;
    private DefaultTableModel model;

    public CategorieView(MainFrame mainFrame) {
        
        this.categorieDAO = new CategorieDAO();

        initComponents();
        setupLayout();
        loadTable();
    }

    private void initComponents() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Description"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCategories = new JTable(model);
        TableUi.apply(tblCategories);
        tblCategories.setAutoCreateRowSorter(true);
        tblCategories.setAutoCreateRowSorter(true);
        tblCategories.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Gestion des Catégories");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

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

        btnAjouter.addActionListener(e -> ouvrirDialog(null));
        btnModifier.addActionListener(e -> modifier());
        btnSupprimer.addActionListener(e -> supprimer());
        btnActualiser.addActionListener(e -> loadTable());

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(lblTitle, BorderLayout.NORTH);
        panelTop.add(panelBoutons, BorderLayout.CENTER);

        add(panelTop, BorderLayout.NORTH);
        add(new JScrollPane(tblCategories), BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadTable() {
        SwingWorker<List<Categorie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Categorie> doInBackground() {
                return categorieDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Categorie> categories = get();
                    model.setRowCount(0);
                    for (Categorie c : categories) {
                        model.addRow(new Object[]{
                                c.getId(), c.getNom(),
                                c.getDescription() != null ? c.getDescription() : ""
                        });
                    }
                } catch (InterruptedException| ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement");
                }
            }
        };
        worker.execute();
    }

    private void modifier() {
        int row = tblCategories.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner une catégorie.");
            return;
        }
        row = tblCategories.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 0);
        Categorie c = new Categorie(id,
                String.valueOf(model.getValueAt(row, 1)),
                String.valueOf(model.getValueAt(row, 2)));
        ouvrirDialog(c);
    }

    private void supprimer() {
        int row = tblCategories.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner une catégorie.");
            return;
        }
        row = tblCategories.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);
        if (DialogUtils.showConfirmation(
                "Voulez-vous vraiment supprimer la catégorie :\n" + nom + " ?")) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return categorieDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            DialogUtils.showInformation("Catégorie supprimée.");
                            loadTable();
                        } else {
                            DialogUtils.showError(
                                "Impossible de supprimer : des médicaments utilisent cette catégorie.");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        DialogUtils.showErrorMessage(e, "Erreur");
                    }
                }
            };
            worker.execute();
        }
    }

    private void ouvrirDialog(Categorie categorie) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String title = categorie == null ? "Ajouter une catégorie" : "Modifier la catégorie";
        CategorieDialog dialog = new CategorieDialog(parent, title, categorie);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTable();
        }
    }

    private class CategorieDialog extends JDialog {

        private boolean confirmed = false;
        private int editingId = 0;
        private JButton btnOK;
        private JTextField txtNom;
        private JTextArea txtDescription;

        public CategorieDialog(Frame parent, String title, Categorie categorie) {
            super(parent, title, true);
            initComponents();
            if (categorie != null) {
                editingId = categorie.getId();
                txtNom.setText(categorie.getNom());
                txtDescription.setText(categorie.getDescription());
            }
            setSize(500, 320);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            txtNom = new JTextField(30);
            txtDescription = new JTextArea(5, 30);
            txtDescription.setLineWrap(true);
            txtDescription.setWrapStyleWord(true);

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Nom * :"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            panel.add(txtNom, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            panel.add(new JLabel("Description :"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane descriptionScrollPane = new JScrollPane(txtDescription);
            descriptionScrollPane.setPreferredSize(new java.awt.Dimension(330, 110));
            panel.add(descriptionScrollPane, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
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

        private void onOK() {
            FormValidation.clear(txtNom);
            if (txtNom.getText().trim().isEmpty()) {
                FormValidation.invalid(txtNom, "Le nom est obligatoire.");
                return;
            }
            if (txtNom.getText().trim().length() > 100) {
                FormValidation.invalid(txtNom, "Le nom ne peut pas dépasser 100 caractères.");
                return;
            }
            Categorie c = new Categorie();
            c.setNom(txtNom.getText().trim());
            c.setDescription(txtDescription.getText().trim());

            if (editingId > 0) {
                c.setId(editingId);
            }
            btnOK.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return editingId > 0 ? categorieDAO.update(c) : categorieDAO.insert(c);
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
