package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.pharmacie.dao.ClientDAO;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.dao.VenteDAO;
import com.pharmacie.models.Client;
import com.pharmacie.models.Medicament;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.models.Vente;
import com.pharmacie.models.VenteDetail;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.FormatUtils;
import com.pharmacie.utils.TableUi;
import com.pharmacie.utils.FormValidation;

public class VenteView extends JPanel {

    private final Utilisateur utilisateur;

    private final MedicamentDAO medicamentDAO;
    private final ClientDAO clientDAO;
    private final VenteDAO venteDAO;

    private JTextField txtRecherche;
    private JComboBox<Client> cmbClient;
    private JTable tblResultats;
    private JTable tblPanier;
    private DefaultTableModel modelResultats;
    private DefaultTableModel modelPanier;

    private JLabel lblTotalArticles;
    private JLabel lblMontantTotal;
    private JLabel lblRemise;
    private JTextField txtRemise;
    private JLabel lblNetPayer;
    private JLabel lblRechercheStatus;

    private final  List<VenteDetail> panier;
    private final Map<Integer, Integer> stockRecherche = new HashMap<>();
    private int rechercheGeneration;
    private boolean venteEnCours;

    public VenteView(MainFrame mainFrame, Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.medicamentDAO = new MedicamentDAO();
        this.clientDAO = new ClientDAO();
        this.venteDAO = new VenteDAO();
        this.panier = new ArrayList<>();

        initComponents();
        setupLayout();
        loadClients();
    }

    private void initComponents() {
        txtRecherche = new JTextField(20);
        txtRecherche.putClientProperty("JTextField.placeholderText", "Nom ou code-barres…");
        cmbClient = new JComboBox<>();
        lblTotalArticles = new JLabel("0");
        lblMontantTotal = new JLabel("0,00 Ar");
        lblRemise = new JLabel("0,00 Ar");
        txtRemise = new JTextField("0", 5);
        lblNetPayer = new JLabel("0,00 Ar");
        lblRechercheStatus = new JLabel("Saisissez un nom ou un code-barres pour rechercher.");

        // Table des résultats de recherche
        modelResultats = new DefaultTableModel(
                new Object[]{"ID", "Médicament", "Prix de vente", "Stock", "Seuil", "Catégorie"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblResultats = new JTable(modelResultats);
        TableUi.apply(tblResultats);
        tblResultats.setAutoCreateRowSorter(true);
        tblResultats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblResultats.removeColumn(tblResultats.getColumnModel().getColumn(0)); // ID interne
        tblResultats.removeColumn(tblResultats.getColumnModel().getColumn(3)); // Seuil utilisé pour l'alerte
        tblResultats.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                int stock = (int) modelResultats.getValueAt(modelRow, 3);
                int threshold = (int) modelResultats.getValueAt(modelRow, 4);
                if (!selected && stock <= threshold) {
                    cell.setBackground(stock == 0 ? new Color(255, 235, 238) : new Color(255, 248, 225));
                    cell.setForeground(stock == 0 ? new Color(183, 28, 28) : new Color(130, 90, 0));
                } else if (!selected) {
                    cell.setBackground(table.getBackground());
                    cell.setForeground(table.getForeground());
                }
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return cell;
            }
        });

        // Table du panier
        modelPanier = new DefaultTableModel(
                new Object[]{"Médicament", "Prix Unitaire", "Quantité", "Sous-Total"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class;
                return Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        tblPanier = new JTable(modelPanier);
        TableUi.apply(tblPanier);
        tblPanier.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Titre ---
        JLabel lblTitle = new JLabel("Point de Vente");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // --- Panel info client ---
        JPanel panelClient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelClient.add(new JLabel("Client :"));
        cmbClient.setPrototypeDisplayValue(new Client(0, "---", "Anonyme", null, null, null));
        cmbClient.setPreferredSize(new Dimension(250, 30));
        panelClient.add(cmbClient);

        // --- Panel gauche : Recherche ---
        JPanel panelGauche = new JPanel(new BorderLayout(5, 5));
        panelGauche.setBorder(BorderFactory.createTitledBorder("Rechercher un médicament"));

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.add(txtRecherche);
        JButton btnRechercher = new JButton("Rechercher");
        panelSearch.add(btnRechercher);
        panelGauche.add(panelSearch, BorderLayout.NORTH);

        JScrollPane scrollResultats = new JScrollPane(tblResultats);
        scrollResultats.setPreferredSize(new Dimension(0, 350));
        panelGauche.add(scrollResultats, BorderLayout.CENTER);

        // Double-clic pour ajouter au panier
        JLabel lblHint = new JLabel("Double-cliquez sur un médicament pour l'ajouter au panier");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        JPanel panelSearchFooter = new JPanel(new BorderLayout(4, 4));
        panelSearchFooter.add(lblHint, BorderLayout.NORTH);
        panelSearchFooter.add(lblRechercheStatus, BorderLayout.SOUTH);
        panelGauche.add(panelSearchFooter, BorderLayout.SOUTH);

        // --- Panel droit : Panier ---
        JPanel panelDroit = new JPanel(new BorderLayout(5, 5));
        panelDroit.setBorder(BorderFactory.createTitledBorder("Panier"));

        JScrollPane scrollPanier = new JScrollPane(tblPanier);
        scrollPanier.setPreferredSize(new Dimension(0, 300));
        panelDroit.add(scrollPanier, BorderLayout.CENTER);

        // --- Panel résumé ---
        JPanel panelResume = new JPanel(new GridBagLayout());
        panelResume.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Résumé"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelResume.add(new JLabel("Total articles :"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        lblTotalArticles.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelResume.add(lblTotalArticles, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelResume.add(new JLabel("Montant total :"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        lblMontantTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelResume.add(lblMontantTotal, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelResume.add(new JLabel("Remise (%) :"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        JPanel panelRemise = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelRemise.add(txtRemise);
        JButton btnAppliquerRemise = new JButton("Appliquer");
        panelRemise.add(btnAppliquerRemise);
        panelResume.add(panelRemise, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panelResume.add(new JLabel("Remise :"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        lblRemise.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelResume.add(lblRemise, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panelResume.add(new JLabel("Net à payer :"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        lblNetPayer.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNetPayer.setForeground(new Color(76, 175, 80));
        panelResume.add(lblNetPayer, gbc);

        panelDroit.add(panelResume, BorderLayout.SOUTH);

        // --- Boutons ---
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnEnregistrer = new JButton("Enregistrer la vente");
        btnEnregistrer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnregistrer.setBackground(new Color(0, 150, 136));
        btnEnregistrer.setForeground(Color.WHITE);
        btnEnregistrer.setFocusPainted(false);
        JButton btnVider = new JButton("Vider le panier");
        JButton btnSupprimer = new JButton("Supprimer la sélection");
        panelBoutons.add(btnEnregistrer);
        panelBoutons.add(btnVider);
        panelBoutons.add(btnSupprimer);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelGauche, panelDroit);
        splitPane.setDividerLocation(400);

        // Assemblage
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.add(lblTitle, BorderLayout.NORTH);
        panelTop.add(panelClient, BorderLayout.CENTER);

        add(panelTop, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);

        setupListeners(btnRechercher, btnAppliquerRemise, btnEnregistrer, btnVider, btnSupprimer);
    }

    private void setupListeners(JButton btnRechercher, JButton btnAppliquerRemise,
                                 JButton btnEnregistrer, JButton btnVider, JButton btnSupprimer) {
        // Recherche sur Enter
        txtRecherche.addActionListener(e -> rechercherMedicaments());
        btnRechercher.addActionListener(e -> rechercherMedicaments());

        // Double-clic pour ajouter au panier
        tblResultats.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    ajouterAuPanier();
                }
            }
        });

        // Modification quantité dans le panier
        modelPanier.addTableModelListener(e -> {
            if (e.getColumn() == 2) {
                int row = e.getFirstRow();
                recalculerLigne(row);
                calculerTotaux();
            }
        });

        // Appliquer remise
        btnAppliquerRemise.addActionListener(e -> {
            if (lireRemise() != null) calculerTotaux();
        });

        // Enregistrer vente
        btnEnregistrer.addActionListener(e -> enregistrerVente());

        // Vider panier
        btnVider.addActionListener(e -> {
            if (DialogUtils.showConfirmation("Voulez-vous vider le panier ?")) {
                viderPanier();
            }
        });

        // Supprimer sélection
        btnSupprimer.addActionListener(e -> supprimerSelection());
    }

    private void loadClients() {
        cmbClient.removeAllItems();
        cmbClient.addItem(new Client(0, "---", "Anonyme", null, null, null));
        new SwingWorker<List<Client>, Void>() {
            @Override protected List<Client> doInBackground() { return clientDAO.findAll(); }
            @Override protected void done() {
                try {
                    for (Client c : get()) cmbClient.addItem(c);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur de chargement des clients");
                }
            }
        }.execute();
    }

    private void rechercherMedicaments() {
        String keyword = txtRecherche.getText().trim();
        int generation = ++rechercheGeneration;
        if (keyword.isEmpty()) {
            modelResultats.setRowCount(0);
            lblRechercheStatus.setText("Saisissez un nom ou un code-barres pour rechercher.");
            return;
        }
        lblRechercheStatus.setText("Recherche en cours…");
        new SwingWorker<List<Medicament>, Void>() {
            @Override protected List<Medicament> doInBackground() {
                return medicamentDAO.searchByNom(keyword);
            }
            @Override protected void done() {
                if (generation != rechercheGeneration) return;
                try {
                    modelResultats.setRowCount(0);
                    for (Medicament m : get()) {
                        stockRecherche.put(m.getId(), m.getQuantiteStock());
                        modelResultats.addRow(new Object[]{
                                m.getId(), m.getNom(), FormatUtils.formatCurrency(m.getPrixVente()),
                                m.getQuantiteStock(), m.getSeuilAlerte(),
                                m.getCategorieNom() != null ? m.getCategorieNom() : ""
                        });
                    }
                    int count = modelResultats.getRowCount();
                    lblRechercheStatus.setText(count == 0
                            ? "Aucun médicament correspondant."
                            : count + " médicament(s) trouvé(s).");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    lblRechercheStatus.setText("Recherche impossible. Vérifiez la connexion.");
                    DialogUtils.showErrorMessage(e, "Erreur de recherche des médicaments");
                }
            }
        }.execute();
    }

    private void ajouterAuPanier() {
        int selectedViewRow = tblResultats.getSelectedRow();
        if (selectedViewRow < 0) return;
        int selectedRow = tblResultats.convertRowIndexToModel(selectedViewRow);

        int medicamentId = (int) modelResultats.getValueAt(selectedRow, 0);
        String nom = (String) modelResultats.getValueAt(selectedRow, 1);
        BigDecimal prixVente = FormatUtils.parseCurrency(modelResultats.getValueAt(selectedRow, 2).toString());
        int stockDispo = (int) modelResultats.getValueAt(selectedRow, 3);

        // Vérifier stock
        int quantiteDansPanier = getQuantiteDansPanier(medicamentId);
        if (quantiteDansPanier >= stockDispo) {
            DialogUtils.showWarning("Stock insuffisant pour " + nom
                    + " (disponible : " + stockDispo + ")");
            return;
        }

        // Vérifier si déjà dans le panier
        int indexExistant = trouverIndexPanier(medicamentId);
        if (indexExistant >= 0) {
            int qty = (int) modelPanier.getValueAt(indexExistant, 2) + 1;
            modelPanier.setValueAt(qty, indexExistant, 2);
            recalculerLigne(indexExistant);
        } else {
            modelPanier.addRow(new Object[]{
                    nom, FormatUtils.formatCurrency(prixVente), 1,
                    FormatUtils.formatCurrency(prixVente)
            });
            panier.add(new VenteDetail(medicamentId, 1, prixVente, prixVente));
        }

        calculerTotaux();
    }

    private int getQuantiteDansPanier(int medicamentId) {
        for (VenteDetail d : panier) {
            if (d.getMedicamentId() == medicamentId) return d.getQuantite();
        }
        return 0;
    }

    private int trouverIndexPanier(int medicamentId) {
        for (int i = 0; i < panier.size(); i++) {
            if (panier.get(i).getMedicamentId() == medicamentId) return i;
        }
        return -1;
    }

    private void recalculerLigne(int row) {
        int qty = (int) modelPanier.getValueAt(row, 2);
        if (qty <= 0) return;
        BigDecimal prix = FormatUtils.parseCurrency(modelPanier.getValueAt(row, 1).toString());
        BigDecimal sousTotal = prix.multiply(BigDecimal.valueOf(qty));
        modelPanier.setValueAt(FormatUtils.formatCurrency(sousTotal), row, 3);

        if (row < panier.size()) {
            VenteDetail detail = panier.get(row);
            detail.setQuantite(qty);
            detail.setSousTotal(sousTotal);
        }
    }

    private void calculerTotaux() {
        BigDecimal total = BigDecimal.ZERO;
        int totalArticles = 0;

        for (int i = 0; i < modelPanier.getRowCount(); i++) {
            total = total.add(FormatUtils.parseCurrency(modelPanier.getValueAt(i, 3).toString()));
            totalArticles += (int) modelPanier.getValueAt(i, 2);
        }

        double remisePercent = 0;
        try {
            remisePercent = Double.parseDouble(txtRemise.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {}

        BigDecimal remise = total.multiply(BigDecimal.valueOf(remisePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal net = total.subtract(remise);

        lblTotalArticles.setText(String.valueOf(totalArticles));
        lblMontantTotal.setText(FormatUtils.formatCurrency(total));
        lblRemise.setText(FormatUtils.formatCurrency(remise));
        lblNetPayer.setText(FormatUtils.formatCurrency(net));
    }

    private void supprimerSelection() {
        int row = tblPanier.getSelectedRow();
        if (row >= 0) {
            modelPanier.removeRow(row);
            if (row < panier.size()) {
                panier.remove(row);
            }
            calculerTotaux();
        }
    }

    private void viderPanier() {
        modelPanier.setRowCount(0);
        panier.clear();
        txtRemise.setText("0");
        calculerTotaux();
    }

    private void enregistrerVente() {
        if (venteEnCours) return;
        if (panier.isEmpty()) {
            DialogUtils.showWarning("Le panier est vide !");
            return;
        }

        Double remisePercent = lireRemise();
        if (remisePercent == null) {
            return;
        }

        for (int row = 0; row < modelPanier.getRowCount(); row++) {
            int quantity;
            try {
                Object value = modelPanier.getValueAt(row, 2);
                if (value instanceof Number number) quantity = number.intValue();
                else if (value != null && value.toString().trim().matches("\\d+")) {
                    quantity = Integer.parseInt(value.toString().trim());
                } else throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                validerQuantitePanier(row, "La quantité doit être un entier supérieur à zéro.");
                return;
            }
            if (quantity <= 0) {
                validerQuantitePanier(row, "La quantité doit être supérieure à zéro.");
                return;
            }
            int stock = stockRecherche.getOrDefault(panier.get(row).getMedicamentId(), 0);
            if (quantity > stock) {
                validerQuantitePanier(row, "Stock insuffisant : " + stock + " unité(s) disponible(s).");
                return;
            }
        }

        if (!DialogUtils.showConfirmation("Confirmer la vente ?")) {
            return;
        }

        // Recalculer les totaux à jour
        recalculerDetails();

        // Créer l'objet Vente
        Client selectedClient = (Client) cmbClient.getSelectedItem();
        int clientId = (selectedClient != null && selectedClient.getId() != 0) ? selectedClient.getId() : 0;

        BigDecimal total = BigDecimal.ZERO;
        for (VenteDetail d : panier) {
            total = total.add(d.getSousTotal());
        }
        BigDecimal remise = total.multiply(BigDecimal.valueOf(remisePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal net = total.subtract(remise);

        Vente vente = new Vente(clientId, utilisateur.getId(), net,
                BigDecimal.valueOf(remisePercent));
        for (VenteDetail d : panier) {
            d.setVenteId(0); // sera mis à jour par le DAO
            vente.addDetail(d);
        }

        // Sauvegarder dans un thread séparé
        venteEnCours = true;
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return venteDAO.insert(vente);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        DialogUtils.showInformation("""
                                                    Vente enregistr\u00e9e avec succ\u00e8s !
                                                    Net \u00e0 payer : """ + FormatUtils.formatCurrency(net));
                        viderPanier();
                        rechercherMedicaments();
                    } else {
                        DialogUtils.showError("Erreur lors de l'enregistrement de la vente.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur lors de la vente");
                } finally {
                    venteEnCours = false;
                }
            }
        };
        worker.execute();
    }

    private void validerQuantitePanier(int row, String message) {
        tblPanier.changeSelection(row, 2, false, false);
        tblPanier.scrollRectToVisible(tblPanier.getCellRect(row, 2, true));
        DialogUtils.showWarning(message);
        tblPanier.editCellAt(row, 2);
        tblPanier.requestFocusInWindow();
    }

    private Double lireRemise() {
        FormValidation.clear(txtRemise);
        try {
            String value = txtRemise.getText().trim().replace(',', '.');
            if (value.matches("\\d+(\\.\\d{1,2})?")) {
                BigDecimal remise = new BigDecimal(value);
                if (remise.compareTo(BigDecimal.ZERO) >= 0
                        && remise.compareTo(BigDecimal.valueOf(100)) <= 0) {
                    return remise.doubleValue();
                }
            }
        } catch (NumberFormatException ignored) {
            // Le message ci-dessous explique la correction attendue.
        }
        FormValidation.invalid(txtRemise, "La remise doit être comprise entre 0 et 100 % (deux décimales maximum).");
        return null;
    }

    private void recalculerDetails() {
        for (int i = 0; i < panier.size(); i++) {
            BigDecimal prix = FormatUtils.parseCurrency(
                    modelPanier.getValueAt(i, 1).toString());
            int qty = (int) modelPanier.getValueAt(i, 2);
            panier.get(i).setQuantite(qty);
            panier.get(i).setPrixUnitaire(prix);
            panier.get(i).setSousTotal(prix.multiply(BigDecimal.valueOf(qty)));
        }
    }
}
