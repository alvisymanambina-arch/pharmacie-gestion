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
        tblCategories.setRowHeight(26);
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
        int id = (int) model.getValueAt(row, 0);
        Categorie c = categorieDAO.findById(id);
        if (c != null) {
            ouvrirDialog(c);
        }
    }

    private void supprimer() {
        int row = tblCategories.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner une catégorie.");
            return;
        }
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

        private void onOK() {
            if (txtNom.getText().trim().isEmpty()) {
                DialogUtils.showWarning("Le nom est obligatoire.");
                return;
            }
            Categorie c = new Categorie();
            c.setNom(txtNom.getText().trim());
            c.setDescription(txtDescription.getText().trim());

            boolean success;
            if (editingId > 0) {
                c.setId(editingId);
                success = categorieDAO.update(c);
            } else {
                success = categorieDAO.insert(c);
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
