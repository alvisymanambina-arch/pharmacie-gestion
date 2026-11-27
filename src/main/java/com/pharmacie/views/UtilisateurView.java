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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.mindrot.jbcrypt.BCrypt;

import com.pharmacie.config.AppConstants;
import com.pharmacie.dao.UtilisateurDAO;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.utils.DateUtils;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.TableUi;
import com.pharmacie.utils.FormValidation;

public class UtilisateurView extends JPanel {

    private final MainFrame mainFrame;
    private final UtilisateurDAO utilisateurDAO;

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
        TableUi.apply(tblUtilisateurs);
        tblUtilisateurs.setAutoCreateRowSorter(true);
        tblUtilisateurs.setAutoCreateRowSorter(true);
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
                } catch (InterruptedException | ExecutionException e) {
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
        row = tblUtilisateurs.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(row, 0);
        new SwingWorker<Utilisateur, Void>() {
            @Override
            protected Utilisateur doInBackground() {
                return utilisateurDAO.findById(id);
            }

            @Override
            protected void done() {
                try {
                    Utilisateur utilisateur = get();
                    if (utilisateur != null) ouvrirDialog(utilisateur);
                    else DialogUtils.showError("Utilisateur introuvable.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement de l'utilisateur");
                }
            }
        }.execute();
    }

    private void supprimer() {
        int row = tblUtilisateurs.getSelectedRow();
        if (row < 0) {
            DialogUtils.showWarning("Veuillez sélectionner un utilisateur.");
            return;
        }
        row = tblUtilisateurs.convertRowIndexToModel(row);
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
                    } catch (InterruptedException | ExecutionException e) {
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
        private JButton btnOK;
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
            FormValidation.clear(txtNom, txtPrenom, txtUsername, txtPassword, cmbRole);
            if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty()
                    || txtUsername.getText().trim().isEmpty()) {
                JComponent missing = txtNom.getText().trim().isEmpty() ? txtNom
                        : txtPrenom.getText().trim().isEmpty() ? txtPrenom : txtUsername;
                FormValidation.invalid(missing, "Nom, prénom et nom d'utilisateur sont obligatoires.");
                return;
            }
            if (txtNom.getText().trim().length() > 100 || txtPrenom.getText().trim().length() > 100) {
                JComponent tooLong = txtNom.getText().trim().length() > 100 ? txtNom : txtPrenom;
                FormValidation.invalid(tooLong, "Le nom et le prénom sont limités à 100 caractères.");
                return;
            }
            if (!txtUsername.getText().trim().matches("[A-Za-z0-9._-]{3,50}")) {
                FormValidation.invalid(txtUsername,
                        "Le nom d'utilisateur doit faire 3 à 50 caractères (lettres, chiffres, point, tiret ou souligné).");
                return;
            }
            char[] passwordChars = txtPassword.getPassword();
            if (editingId == 0 && passwordChars.length == 0) {
                FormValidation.invalid(txtPassword, "Le mot de passe est obligatoire.");
                return;
            }
            if (passwordChars.length > 0 && passwordChars.length < 8) {
                FormValidation.invalid(txtPassword, "Le mot de passe doit contenir au moins 8 caractères.");
                return;
            }

            Utilisateur u = new Utilisateur();
            u.setNom(txtNom.getText().trim());
            u.setPrenom(txtPrenom.getText().trim());
            u.setUsername(txtUsername.getText().trim());
            u.setRole((String) cmbRole.getSelectedItem());
            u.setActif(chkActif.isSelected());

            String password = new String(passwordChars);
            java.util.Arrays.fill(passwordChars, '\0');
            if (!password.isEmpty()) {
                u.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            }

            boolean withPassword = !password.isEmpty();
            if (editingId > 0) {
                u.setId(editingId);
            }
            btnOK.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    if (editingId == 0) return utilisateurDAO.insert(u);
                    return withPassword
                            ? utilisateurDAO.updateWithPassword(u)
                            : utilisateurDAO.update(u);
                }

                @Override
                protected void done() {
                    btnOK.setEnabled(true);
                    try {
                        if (get()) {
                            confirmed = true;
                            dispose();
                        } else {
                            DialogUtils.showError("Erreur lors de l'enregistrement. "
                                    + "Ce nom d'utilisateur existe peut-être déjà.");
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

        public boolean isConfirmed() { return confirmed; }
    }
}
