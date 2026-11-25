package com.pharmacie.views;

import com.pharmacie.config.AppConstants;
import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.utils.DateUtils;
import com.pharmacie.utils.DialogUtils;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UtilisateurView extends JPanel {

    private MainFrame mainFrame;
    private UtilisateurDAO utilisateurDAO;

    private JTable tblUtilisateurs;
    private DefaultTableModel model;

    public UtilisateurView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.utilisateurDAO = new UtilisateurDAO();

        initComponents();
        setupLayout();
        loadTable();
    }

    private void initComponents() {
        model = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Prénom", "Username", "Rôle", "Actif",
                        "Date Création"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblUtilisateurs = new JTable(model);
        tblUtilisateurs.setRowHeight(26);
        tblUtilisateurs.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Gestion des Utilisateurs");
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
        add(new JScrollPane(tblUtilisateurs), BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadTable() {
        SwingWorker<List<Utilisateur>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Utilisateur> doInBackground() {
                return utilisateurDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Utilisateur> users = get();
                    model.setRowCount(0);
                    for (Utilisateur u : users) {
                        model.addRow(new Object[]{
                                u.getId(), u.getNom(), u.getPrenom(),
                                u.getUsername(), u.getRole(),
                                u.isActif() ? "Oui" : "Non",
                                DateUtils.formatDateTime(u.getDateCreation())
                        });
                    }
                } catch (Exception e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }

    private void modifier() {
        int row = tblUtilisateurs.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un utilisateur.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Utilisateur u = utilisateurDAO.findById(id);
        if (u != null) {
            ouvrirDialog(u);
        }
    }

    private void supprimer() {
        int row = tblUtilisateurs.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un utilisateur.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String nom = model.getValueAt(row, 1) + " " + model.getValueAt(row, 2);
        if (id == mainFrame.getUtilisateur().getId()) {
            DialogUtils.showWarning("Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }
        if (DialogUtils.showConfirmation(
                "Voulez-vous vraiment supprimer l'utilisateur :\n" + nom + " ?")) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return utilisateurDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            DialogUtils.showInformation("Utilisateur supprimé.");
                            loadTable();
                        } else {
                            DialogUtils.showError("Impossible de supprimer cet utilisateur.");
                        }
                    } catch (Exception e) {
                        DialogUtils.showErrorMessage(e, "Erreur");
                    }
                }
            };
            worker.execute();
        }
    }

    private void ouvrirDialog(Utilisateur utilisateur) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String title = utilisateur == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur";
        UtilisateurDialog dialog = new UtilisateurDialog(parent, title, utilisateur);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTable();
        }
    }

    private class UtilisateurDialog extends JDialog {

        private boolean confirmed = false;
        private int editingId = 0;
        private JTextField txtNom, txtPrenom, txtUsername;
        private JPasswordField txtPassword;
        private PasswordFieldPanel passwordPanel;
        private JComboBox<String> cmbRole;
        private JCheckBox chkActif;

        public UtilisateurDialog(Frame parent, String title, Utilisateur utilisateur) {
            super(parent, title, true);
            initComponents();
            if (utilisateur != null) {
                editingId = utilisateur.getId();
                txtNom.setText(utilisateur.getNom());
                txtPrenom.setText(utilisateur.getPrenom());
                txtUsername.setText(utilisateur.getUsername());
                cmbRole.setSelectedItem(utilisateur.getRole());
                chkActif.setSelected(utilisateur.isActif());
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
            txtPrenom = new JTextField(25);
            txtUsername = new JTextField(25);
            txtPassword = new JPasswordField(25);
            passwordPanel = new PasswordFieldPanel(txtPassword);
            cmbRole = new JComboBox<>(new String[]{
                    AppConstants.ROLE_ADMIN, AppConstants.ROLE_PHARMACIEN
            });
            chkActif = new JCheckBox("Compte actif");

            int y = 0;
            addRow(panel, gbc, "Nom * :", txtNom, y++);
            addRow(panel, gbc, "Prénom * :", txtPrenom, y++);
            addRow(panel, gbc, "Nom d'utilisateur * :", txtUsername, y++);
            JLabel lblPwdHint = new JLabel(editingId > 0
                    ? "Mot de passe (vide = inchangé) :"
                    : "Mot de passe * :");
            addRow(panel, gbc, lblPwdHint.getText(), passwordPanel, y++);
            addRow(panel, gbc, "Rôle * :", cmbRole, y++);

            gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
            panel.add(chkActif, gbc);

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
            if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()
                    || txtUsername.getText().trim().isEmpty()) {
                DialogUtils.showWarning("Nom, prénom et nom d'utilisateur sont obligatoires.");
                return;
            }
            if (editingId == 0 && txtPassword.getPassword().length == 0) {
                DialogUtils.showWarning("Le mot de passe est obligatoire.");
                return;
            }

            Utilisateur u = new Utilisateur();
            u.setNom(txtNom.getText().trim());
            u.setPrenom(txtPrenom.getText().trim());
            u.setUsername(txtUsername.getText().trim());
            u.setRole((String) cmbRole.getSelectedItem());
            u.setActif(chkActif.isSelected());

            String password = new String(txtPassword.getPassword());
            if (!password.isEmpty()) {
                u.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            }

            boolean success;
            if (editingId > 0) {
                u.setId(editingId);
                // Un champ vide conserve le mot de passe actuel.
                if (password.isEmpty()) {
                    Utilisateur existing = utilisateurDAO.findById(editingId);
                    if (existing != null) {
                        u.setPassword(existing.getPassword());
                    }
                    success = utilisateurDAO.update(u);
                } else {
                    success = utilisateurDAO.updateWithPassword(u);
                }
            } else {
                success = utilisateurDAO.insert(u);
            }

            if (success) {
                confirmed = true;
                dispose();
            } else {
                DialogUtils.showError("Erreur lors de l'enregistrement. "
                        + "Ce nom d'utilisateur existe peut-être déjà.");
            }
        }

        public boolean isConfirmed() { return confirmed; }
    }
}
