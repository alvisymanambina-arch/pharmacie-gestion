package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.pharmacie.dao.ApprovisionnementDAO;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.dao.VenteDAO;
import com.pharmacie.models.Approvisionnement;
import com.pharmacie.models.Medicament;
import com.pharmacie.models.Vente;
import com.pharmacie.utils.DateUtils;
import com.pharmacie.utils.FormatUtils;

public class RapportView extends JPanel {


    private final VenteDAO venteDAO;
    private final MedicamentDAO medicamentDAO;
    private final ApprovisionnementDAO approvisionnementDAO;

    // Tab 1 : Ventes
    private JFormattedTextField txtDateDebutVente;
    private JFormattedTextField txtDateFinVente;
    private JTable tblVentes;
    private DefaultTableModel modelVentes;
    private JLabel lblTotalVentes;

    // Tab 2 : Stock
    private JTable tblStock;
    private DefaultTableModel modelStock;
    private JLabel lblValeurStock;

    // Tab 3 : Approvisionnements
    private JFormattedTextField txtDateDebutAppro;
    private JFormattedTextField txtDateFinAppro;
    private JTable tblApprovisionnements;
    private DefaultTableModel modelAppro;
    private JLabel lblTotalAppro;

    public RapportView(MainFrame mainFrame) {
        this.venteDAO = new VenteDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.approvisionnementDAO = new ApprovisionnementDAO();

        initComponents();
        setupLayout();
    }

    private void initComponents() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Tab Ventes
        txtDateDebutVente = new JFormattedTextField(sdf);
        txtDateFinVente = new JFormattedTextField(sdf);
        txtDateDebutVente.setColumns(10);
        txtDateFinVente.setColumns(10);
        txtDateDebutVente.setValue(java.sql.Date.valueOf(LocalDate.now().withDayOfMonth(1)));
        txtDateFinVente.setValue(java.sql.Date.valueOf(LocalDate.now()));

        modelVentes = new DefaultTableModel(
                new Object[]{"ID", "Date", "Client", "Montant Total", "Remise", "Net"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblVentes = new JTable(modelVentes);
        tblVentes.setRowHeight(25);
        lblTotalVentes = new JLabel("Total : 0,00 Ar");

        // Tab Stock
        modelStock = new DefaultTableModel(
                new Object[]{"Nom", "Catégorie", "Stock", "Prix Achat", "Prix Vente", "Valeur Stock"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStock = new JTable(modelStock);
        tblStock.setRowHeight(25);
        lblValeurStock = new JLabel("Valeur totale du stock : 0,00 Ar");

        // Tab Approvisionnements
        txtDateDebutAppro = new JFormattedTextField(sdf);
        txtDateFinAppro = new JFormattedTextField(sdf);
        txtDateDebutAppro.setColumns(10);
        txtDateFinAppro.setColumns(10);
        txtDateDebutAppro.setValue(java.sql.Date.valueOf(LocalDate.now().withDayOfMonth(1)));
        txtDateFinAppro.setValue(java.sql.Date.valueOf(LocalDate.now()));

        modelAppro = new DefaultTableModel(
                new Object[]{"ID", "Date", "Fournisseur", "Montant Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblApprovisionnements = new JTable(modelAppro);
        tblApprovisionnements.setRowHeight(25);
        lblTotalAppro = new JLabel("Total : 0,00 Ar");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Rapports et Statistiques");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Ventes", createTabVentes());
        tabbedPane.addTab("Stock", createTabStock());
        tabbedPane.addTab("Approvisionnements", createTabAppro());
        add(tabbedPane, BorderLayout.CENTER);

        // Charger les données au démarrage
        SwingUtilities.invokeLater(() -> {
            chargerStock();
        });
    }

    private JPanel createTabVentes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Barre de filtres
        JPanel panelFiltres = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltres.add(new JLabel("Date début :"));
        panelFiltres.add(txtDateDebutVente);
        panelFiltres.add(new JLabel("Date fin :"));
        panelFiltres.add(txtDateFinVente);
        JButton btnGenerer = new JButton("Générer");
        btnGenerer.addActionListener(e -> chargerVentes());
        panelFiltres.add(btnGenerer);
        panel.add(panelFiltres, BorderLayout.NORTH);

        // Table
        JScrollPane scrollPane = new JScrollPane(tblVentes);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Résumé
        JPanel panelResume = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalVentes.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelResume.add(lblTotalVentes);
        panel.add(panelResume, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTabStock() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnActualiser = new JButton("Actualiser");
        btnActualiser.addActionListener(e -> chargerStock());
        panelHaut.add(btnActualiser);
        panel.add(panelHaut, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tblStock);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelResume = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblValeurStock.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValeurStock.setForeground(new Color(76, 175, 80));
        panelResume.add(lblValeurStock);
        panel.add(panelResume, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTabAppro() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelFiltres = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltres.add(new JLabel("Date début :"));
        panelFiltres.add(txtDateDebutAppro);
        panelFiltres.add(new JLabel("Date fin :"));
        panelFiltres.add(txtDateFinAppro);
        JButton btnGenerer = new JButton("Générer");
        btnGenerer.addActionListener(e -> chargerAppro());
        panelFiltres.add(btnGenerer);
        panel.add(panelFiltres, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tblApprovisionnements);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelResume = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalAppro.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelResume.add(lblTotalAppro);
        panel.add(panelResume, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerVentes() {
        LocalDate debut = DateUtils.parseDate(txtDateDebutVente.getText());
        LocalDate fin = DateUtils.parseDate(txtDateFinVente.getText());

        if (debut == null || fin == null) {
            com.pharmacie.utils.DialogUtils.showWarning("Veuillez saisir des dates valides (dd/MM/yyyy).");
            return;
        }

        SwingWorker<List<Vente>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vente> doInBackground() {
                return venteDAO.findByDateRange(debut, fin);
            }

            @Override
            protected void done() {
                try {
                    List<Vente> ventes = get();
                    modelVentes.setRowCount(0);
                    BigDecimal total = BigDecimal.ZERO;
                    for (Vente v : ventes) {
                        // montant_total est déjà le net à payer, remise est en %
                        BigDecimal remisePct = v.getRemise() != null
                                ? v.getRemise() : BigDecimal.ZERO;
                        // Recalculer le brut : net / (1 - remise/100)
                        BigDecimal brut;
                        if (remisePct.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal denom = BigDecimal.ONE.subtract(
                                    remisePct.divide(BigDecimal.valueOf(100), 6,
                                            java.math.RoundingMode.HALF_UP));
                            brut = v.getMontantTotal().divide(denom, 2,
                                    java.math.RoundingMode.HALF_UP);
                        } else {
                            brut = v.getMontantTotal();
                        }
                        modelVentes.addRow(new Object[]{
                                v.getId(),
                                DateUtils.formatDateTime(v.getDateVente()),
                                v.getClientNom() != null ? v.getClientNom() : "Anonyme",
                                FormatUtils.formatCurrency(brut),
                                remisePct + " %",
                                FormatUtils.formatCurrency(v.getMontantTotal())
                        });
                        total = total.add(v.getMontantTotal());
                    }
                    lblTotalVentes.setText("Total des ventes (net) : "
                            + FormatUtils.formatCurrency(total));
                } catch (InterruptedException | ExecutionException e) {
                    com.pharmacie.utils.DialogUtils.showErrorMessage(e,
                            "Erreur lors du chargement des ventes");
                }
            }
        };
        worker.execute();
    }

    private void chargerStock() {
        SwingWorker<List<Medicament>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Medicament> doInBackground() {
                return medicamentDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Medicament> medicaments = get();
                    modelStock.setRowCount(0);
                    BigDecimal valeurTotale = BigDecimal.ZERO;
                    for (Medicament m : medicaments) {
                        BigDecimal valeurStock = m.getPrixAchat().multiply(
                                BigDecimal.valueOf(m.getQuantiteStock()));
                        valeurTotale = valeurTotale.add(valeurStock);
                        modelStock.addRow(new Object[]{
                                m.getNom(),
                                m.getCategorieNom() != null ? m.getCategorieNom() : "",
                                m.getQuantiteStock(),
                                FormatUtils.formatCurrency(m.getPrixAchat()),
                                FormatUtils.formatCurrency(m.getPrixVente()),
                                FormatUtils.formatCurrency(valeurStock)
                        });
                    }
                    lblValeurStock.setText("Valeur totale du stock : "
                            + FormatUtils.formatCurrency(valeurTotale));
                } catch (InterruptedException | ExecutionException e) {
                    com.pharmacie.utils.DialogUtils.showErrorMessage(e,
                            "Erreur lors du chargement du stock");
                }
            }
        };
        worker.execute();
    }

    private void chargerAppro() {
        LocalDate debut = DateUtils.parseDate(txtDateDebutAppro.getText());
        LocalDate fin = DateUtils.parseDate(txtDateFinAppro.getText());

        if (debut == null || fin == null) {
            com.pharmacie.utils.DialogUtils.showWarning("Veuillez saisir des dates valides (dd/MM/yyyy).");
            return;
        }

        SwingWorker<List<Approvisionnement>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Approvisionnement> doInBackground() {
                return approvisionnementDAO.findByDateRange(debut, fin);
            }

            @Override
            protected void done() {
                try {
                    List<Approvisionnement> appros = get();
                    modelAppro.setRowCount(0);
                    BigDecimal total = BigDecimal.ZERO;
                    for (Approvisionnement a : appros) {
                        modelAppro.addRow(new Object[]{
                                a.getId(),
                                DateUtils.formatDateTime(a.getDateAppro()),
                                a.getFournisseurNom() != null ? a.getFournisseurNom() : "",
                                FormatUtils.formatCurrency(a.getMontantTotal())
                        });
                        total = total.add(a.getMontantTotal());
                    }
                    lblTotalAppro.setText("Total des approvisionnements : "
                            + FormatUtils.formatCurrency(total));
                } catch (InterruptedException | ExecutionException e) {
                    com.pharmacie.utils.DialogUtils.showErrorMessage(e,
                            "Erreur lors du chargement");
                }
            }
        };
        worker.execute();
    }
}
