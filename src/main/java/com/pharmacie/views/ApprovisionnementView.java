package com.pharmacie.views;

import com.pharmacie.dao.ApprovisionnementDAO;
import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.models.Approvisionnement;
import com.pharmacie.models.ApprovisionnementDetail;
import com.pharmacie.models.Fournisseur;
import com.pharmacie.models.Medicament;
import com.pharmacie.models.Utilisateur;
import com.pharmacie.utils.DialogUtils;
import com.pharmacie.utils.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApprovisionnementView extends JPanel {

   
    private final Utilisateur utilisateur;

    private final ApprovisionnementDAO approvisionnementDAO;
    private final FournisseurDAO fournisseurDAO;
    private final MedicamentDAO medicamentDAO;

    private JComboBox<Fournisseur> cmbFournisseur;
    private JTextField txtRecherche;
    private JTable tblResultats;
    private JTable tblCommande;
    private DefaultTableModel modelResultats;
    private DefaultTableModel modelCommande;

    private JLabel lblNombreArticles;
    private JLabel lblMontantTotal;

    private final List<ApprovisionnementDetail> commande;
    private final List<Integer> medicamentIds;

    public ApprovisionnementView(MainFrame mainFrame, Utilisateur utilisateur) {
      
        this.utilisateur = utilisateur;
        this.approvisionnementDAO = new ApprovisionnementDAO();
        this.fournisseurDAO = new FournisseurDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.commande = new ArrayList<>();
        this.medicamentIds = new ArrayList<>();

        initComponents();
        setupLayout();
        loadFournisseurs();
    }

    private void initComponents() {
        cmbFournisseur = new JComboBox<>();
        txtRecherche = new JTextField(20);
        lblNombreArticles = new JLabel("0");
        lblMontantTotal = new JLabel("0,00 Ar");

        // Table des résultats de recherche
        modelResultats = new DefaultTableModel(
                new Object[]{"ID", "Nom", "Stock", "Prix Achat", "Catégorie"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblResultats = new JTable(modelResultats);
        tblResultats.setRowHeight(25);

        // Table de la commande
        modelCommande = new DefaultTableModel(
            new Object[]{"Médicament", "N° Lot", "Expiration (AAAA-MM-JJ)", "Quantité", "Prix Achat", "Sous-Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
            return column >= 1 && column <= 4;
            }
        };
        tblCommande = new JTable(modelCommande);
        tblCommande.setRowHeight(28);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Titre + Fournisseur ---
        JLabel lblTitle = new JLabel("Approvisionnement");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel panelTop = new JPanel(new BorderLayout(10, 5));
        panelTop.add(lblTitle, BorderLayout.NORTH);

        JPanel panelFournisseur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFournisseur.add(new JLabel("Fournisseur :"));
        cmbFournisseur.setPreferredSize(new Dimension(250, 30));
        panelFournisseur.add(cmbFournisseur);
        panelFournisseur.add(Box.createHorizontalStrut(20));
        panelFournisseur.add(new JLabel("Date : "
                + LocalDateTime.now().toLocalDate().toString()));
        panelTop.add(panelFournisseur, BorderLayout.CENTER);

        // --- Panel gauche : Recherche ---
        JPanel panelGauche = new JPanel(new BorderLayout(5, 5));
        panelGauche.setBorder(BorderFactory.createTitledBorder("Rechercher un médicament"));

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.add(txtRecherche);
        JButton btnRechercher = new JButton("Rechercher");
        panelSearch.add(btnRechercher);
        panelGauche.add(panelSearch, BorderLayout.NORTH);

        JScrollPane scrollResultats = new JScrollPane(tblResultats);
        panelGauche.add(scrollResultats, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Double-cliquez sur un médicament pour l'ajouter à la commande");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        panelGauche.add(lblHint, BorderLayout.SOUTH);

        // --- Panel droit : Commande ---
        JPanel panelDroit = new JPanel(new BorderLayout(5, 5));
        panelDroit.setBorder(BorderFactory.createTitledBorder("Commande"));

        JScrollPane scrollCommande = new JScrollPane(tblCommande);
        panelDroit.add(scrollCommande, BorderLayout.CENTER);

        // Résumé
        JPanel panelResume = new JPanel(new GridLayout(1, 2, 20, 5));
        panelResume.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Résumé"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel panelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLeft.add(new JLabel("Nombre d'articles : "));
        lblNombreArticles.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelLeft.add(lblNombreArticles);
        panelResume.add(panelLeft);

        JPanel panelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelRight.add(new JLabel("Montant Total : "));
        lblMontantTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMontantTotal.setForeground(new Color(76, 175, 80));
        panelRight.add(lblMontantTotal);
        panelResume.add(panelRight);

        panelDroit.add(panelResume, BorderLayout.SOUTH);

        // --- Boutons ---
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnEnregistrer = new JButton("Enregistrer l'approvisionnement");
        btnEnregistrer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnregistrer.setBackground(new Color(0, 150, 136));
        btnEnregistrer.setForeground(Color.WHITE);
        JButton btnVider = new JButton("Vider la liste");
        JButton btnSupprimer = new JButton("Supprimer la sélection");
        panelBoutons.add(btnEnregistrer);
        panelBoutons.add(btnVider);
        panelBoutons.add(btnSupprimer);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelGauche, panelDroit);
        splitPane.setDividerLocation(400);

        // Listeners
        btnRechercher.addActionListener(e -> rechercherMedicaments());
        txtRecherche.addActionListener(e -> rechercherMedicaments());

        tblResultats.addMouseListener(new java.awt.event.MouseAdapter() {
            public  void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    ajouterACommande();
                }
            }
        });

        modelCommande.addTableModelListener(e -> {
            int col = e.getColumn();
            if (col >= 1 && col <= 4) {
                recalculerLigne(e.getFirstRow());
                calculerTotaux();
            }
        });

        btnEnregistrer.addActionListener(e -> enregistrer());
        btnVider.addActionListener(e -> {
            if (DialogUtils.showConfirmation("Vider la liste de commande ?")) {
                viderCommande();
            }
        });
        btnSupprimer.addActionListener(e -> supprimerSelection());

        add(panelTop, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);
    }

    private void loadFournisseurs() {
        cmbFournisseur.removeAllItems();
        List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
        for (Fournisseur f : fournisseurs) {
            cmbFournisseur.addItem(f);
        }
        if (cmbFournisseur.getItemCount() == 0) {
            DialogUtils.showWarning("Aucun fournisseur enregistré. Veuillez en ajouter d'abord.");
        }
    }

    private void rechercherMedicaments() {
        String keyword = txtRecherche.getText().trim();
        List<Medicament> medicaments;
        if (keyword.isEmpty()) {
            medicaments = medicamentDAO.findAll();
        } else {
            medicaments = medicamentDAO.searchByNom(keyword);
        }

        modelResultats.setRowCount(0);
        for (Medicament m : medicaments) {
            modelResultats.addRow(new Object[]{
                    m.getId(), m.getNom(), m.getQuantiteStock(),
                    FormatUtils.formatCurrency(m.getPrixAchat()),
                    m.getCategorieNom() != null ? m.getCategorieNom() : ""
            });
        }
    }

    private void ajouterACommande() {
        int selectedRow = tblResultats.getSelectedRow();
        if (selectedRow < 0) return;

        int medicamentId = (int) modelResultats.getValueAt(selectedRow, 0);
        String nom = (String) modelResultats.getValueAt(selectedRow, 1);
        BigDecimal prixAchat = FormatUtils.parseCurrency(
                modelResultats.getValueAt(selectedRow, 3).toString());

        // Vérifier si déjà dans la commande
        int indexExistant = medicamentIds.indexOf(medicamentId);
        if (indexExistant >= 0) {
            int qty = (int) modelCommande.getValueAt(indexExistant, 1) + 1;
            modelCommande.setValueAt(qty, indexExistant, 1);
            recalculerLigne(indexExistant);
        } else {
            modelCommande.addRow(new Object[]{
                    nom, "", "", 1, FormatUtils.formatCurrency(prixAchat),
                    FormatUtils.formatCurrency(prixAchat)
            });
            medicamentIds.add(medicamentId);
            commande.add(new ApprovisionnementDetail(medicamentId, 1, prixAchat, prixAchat));
        }

        calculerTotaux();
    }

    private void recalculerLigne(int row) {
        if (row < 0 || row >= modelCommande.getRowCount()) return;
        int qty = 1;
        try {
            Object val = modelCommande.getValueAt(row, 3);
            if (val instanceof Number) qty = ((Number) val).intValue();
            else qty = Integer.parseInt(val.toString());
        } catch (NumberFormatException ignored) {}
        if (qty <= 0) qty = 1;

        BigDecimal prixAchat = FormatUtils.parseCurrency(
            modelCommande.getValueAt(row, 4).toString());
        BigDecimal sousTotal = prixAchat.multiply(BigDecimal.valueOf(qty));
        modelCommande.setValueAt(FormatUtils.formatCurrency(sousTotal), row, 3);

        if (row < commande.size()) {
            ApprovisionnementDetail d = commande.get(row);
            d.setQuantite(qty);
            d.setPrixAchat(prixAchat);
            d.setSousTotal(sousTotal);
            d.setNumeroLot(String.valueOf(modelCommande.getValueAt(row, 1)).trim());
            String expiration = String.valueOf(modelCommande.getValueAt(row, 2)).trim();
            try {
                d.setDateExpiration(expiration.isBlank() ? null : LocalDate.parse(expiration));
            } catch (RuntimeException ex) {
                d.setDateExpiration(null);
            }
        }
    }

    private void calculerTotaux() {
        BigDecimal total = BigDecimal.ZERO;
        int nbArticles = 0;
        for (ApprovisionnementDetail d : commande) {
            total = total.add(d.getSousTotal());
            nbArticles += d.getQuantite();
        }
        lblNombreArticles.setText(String.valueOf(nbArticles));
        lblMontantTotal.setText(FormatUtils.formatCurrency(total));
    }

    private void supprimerSelection() {
        int row = tblCommande.getSelectedRow();
        if (row >= 0) {
            modelCommande.removeRow(row);
            if (row < commande.size()) commande.remove(row);
            if (row < medicamentIds.size()) medicamentIds.remove(row);
            calculerTotaux();
        }
    }

    private void viderCommande() {
        modelCommande.setRowCount(0);
        commande.clear();
        medicamentIds.clear();
        calculerTotaux();
    }

    private void enregistrer() {
        Fournisseur selectedFournisseur = (Fournisseur) cmbFournisseur.getSelectedItem();
        if (selectedFournisseur == null || selectedFournisseur.getId() == 0) {
            DialogUtils.showWarning("Veuillez sélectionner un fournisseur.");
            return;
        }
        if (commande.isEmpty()) {
            DialogUtils.showWarning("La commande est vide !");
            return;
        }
        if (!DialogUtils.showConfirmation("Confirmer l'approvisionnement ?")) {
            return;
        }

        // Recalculer les détails
        for (int i = 0; i < commande.size(); i++) {
            BigDecimal prixAchat = FormatUtils.parseCurrency(
                    modelCommande.getValueAt(i, 4).toString());
            int qty = 1;
            try {
                Object val = modelCommande.getValueAt(i, 3);
                if (val instanceof Number number) qty = number.intValue();
                else qty = Integer.parseInt(val.toString());
            } catch (NumberFormatException ignored) {}
            commande.get(i).setQuantite(qty);
            commande.get(i).setPrixAchat(prixAchat);
            commande.get(i).setSousTotal(prixAchat.multiply(BigDecimal.valueOf(qty)));
            commande.get(i).setNumeroLot(String.valueOf(modelCommande.getValueAt(i, 1)).trim());
            String expiration = String.valueOf(modelCommande.getValueAt(i, 2)).trim();
            try {
                commande.get(i).setDateExpiration(expiration.isBlank() ? null : LocalDate.parse(expiration));
            } catch (RuntimeException ex) {
                DialogUtils.showWarning("Date d'expiration invalide pour la ligne " + (i + 1)
                        + ". Format attendu : AAAA-MM-JJ.");
                return;
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ApprovisionnementDetail d : commande) {
            total = total.add(d.getSousTotal());
        }

        BigDecimal montantTotal = total;
        Approvisionnement appro = new Approvisionnement(
                selectedFournisseur.getId(), utilisateur.getId(), montantTotal);
        for (ApprovisionnementDetail d : commande) {
            appro.addDetail(d);
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return approvisionnementDAO.insert(appro);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        DialogUtils.showInformation(
                                """
                                Approvisionnement enregistr\u00e9 avec succ\u00e8s !
                                Montant total : """ + FormatUtils.formatCurrency(montantTotal));
                        viderCommande();
                        rechercherMedicaments();
                    } else {
                        DialogUtils.showError(
                                "Erreur lors de l'enregistrement de l'approvisionnement.");
                    }
                } catch (Exception e) {
                    DialogUtils.showErrorMessage(e, "Erreur");
                }
            }
        };
        worker.execute();
    }
}
