package com.pharmacie.views;

import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.dao.VenteDAO;
import com.pharmacie.models.Medicament;
import com.pharmacie.utils.DateUtils;
import com.pharmacie.utils.FormatUtils;
import com.pharmacie.utils.TableUi;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DashboardView extends JPanel {

    
    private final MedicamentDAO medicamentDAO;
    private final VenteDAO venteDAO;

    // Stat labels
    private JLabel lblMedicamentsCount;
    private JLabel lblAlertesCount;
    private JLabel lblChiffreAffaires;
    private JLabel lblVentesCount;

    // Alert table
    private JTable alertTable;
    private DefaultTableModel alertTableModel;

    // Data holders
    private List<Medicament> alerteMedicaments;

    private static final Color CARD_TEAL = new Color(0, 150, 136);
    private static final Color CARD_ORANGE = new Color(255, 152, 0);
    private static final Color CARD_GREEN = new Color(76, 175, 80);
    private static final Color CARD_BLUE = new Color(33, 150, 243);
    private static final Color ROW_ORANGE = new Color(255, 243, 224);
    private static final Color ROW_RED = new Color(255, 235, 238);
    private static final Color ROW_ORANGE_DARK = new Color(80, 50, 0);
    private static final Color ROW_RED_DARK = new Color(120, 20, 20);

    public DashboardView(MainFrame mainFrame) {
        
        this.medicamentDAO = new MedicamentDAO();
        this.venteDAO = new VenteDAO();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(UIManager.getColor("Panel.background"));

        initComponents();
        layoutComponents();

        // Load data asynchronously
        loadData();
    }

    private void initComponents() {
        lblMedicamentsCount = new JLabel("0");
        lblAlertesCount = new JLabel("0");
        lblChiffreAffaires = new JLabel("0 Ar");
        lblVentesCount = new JLabel("0");

        String[] columns = {"Nom", "Cat\u00e9gorie", "Stock", "Seuil", "Date Expiration", "Statut"};
        alertTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        alertTable = new JTable(alertTableModel);
        TableUi.apply(alertTable);
        alertTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        alertTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        alertTable.setFillsViewportHeight(true);
    }

    private void layoutComponents() {
        // Title
        JLabel lblTitle = new JLabel("Tableau de bord");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(UIManager.getColor("Label.foreground"));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Stat cards panel (2x2 grid)
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        cardsPanel.setBackground(UIManager.getColor("Panel.background"));

        cardsPanel.add(createStatCard("\uD83D\uDC8A", "M\u00e9dicaments en stock",
                lblMedicamentsCount, CARD_TEAL));
        cardsPanel.add(createStatCard("\u26A0\uFE0F", "Alertes stock faible",
                lblAlertesCount, CARD_ORANGE));
        cardsPanel.add(createStatCard("\uD83D\uDCB0", "Chiffre d'affaires (jour)",
                lblChiffreAffaires, CARD_GREEN));
        cardsPanel.add(createStatCard("\uD83D\uDED2", "Ventes du jour",
                lblVentesCount, CARD_BLUE));

        // Top section
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getColor("Panel.background"));
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(cardsPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Alert table section
        JPanel alertSection = new JPanel(new BorderLayout(10, 10));
        alertSection.setBackground(UIManager.getColor("Panel.background"));
        alertSection.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblAlertTitle = new JLabel("Alertes : M\u00e9dicaments en stock faible ou expiration proche");
        lblAlertTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAlertTitle.setForeground(UIManager.getColor("Label.foreground"));
        alertSection.add(lblAlertTitle, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(alertTable);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        alertSection.add(scrollPane, BorderLayout.CENTER);

        add(alertSection, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String icon, String description, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        lblIcon.setPreferredSize(new Dimension(60, 60));
        card.add(lblIcon, BorderLayout.WEST);

        // Value and description
        JPanel textPanel = new JPanel(new BorderLayout(5, 0));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        textPanel.add(valueLabel, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(255, 255, 255, 200));
        textPanel.add(lblDesc, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Total medicaments
                    List<Medicament> allMeds = medicamentDAO.findAll();
                    int totalCount = (int) allMeds.stream()
                            .filter(m -> m.getQuantiteStock() > 0).count();

                    // Low stock alerts
                    List<Medicament> lowStock = medicamentDAO.findStockFaible();

                    // Near expiration
                    List<Medicament> nearExp = medicamentDAO.findExpirationsProches();

                    // Today's revenue
                    LocalDate today = LocalDate.now();
                    BigDecimal ca = venteDAO.getChiffreAffairesJour(today);

                    // Today's sales count
                    int venteCount = venteDAO.getNombreVentesJour(today);

                    // Combine alertes (low stock + near expiration, deduplicate)
                    alerteMedicaments = lowStock;
                    for (Medicament m : nearExp) {
                        if (!alerteMedicaments.stream().anyMatch(a -> a.getId() == m.getId())) {
                            alerteMedicaments.add(m);
                        }
                    }

                    final int finalTotal = totalCount;
                    final int finalAlerteCount = alerteMedicaments.size();
                    final String finalCA = ca == null ? "Erreur" : FormatUtils.formatCurrency(ca);
                    final String finalVenteCount = venteCount < 0
                            ? "Erreur" : FormatUtils.formatNumber(venteCount);

                    SwingUtilities.invokeLater(() -> {
                        lblMedicamentsCount.setText(String.valueOf(finalTotal));
                        lblAlertesCount.setText(String.valueOf(finalAlerteCount));
                        lblChiffreAffaires.setText(finalCA);
                        lblVentesCount.setText(finalVenteCount);

                        updateAlertTable();
                    });
                } catch (Exception e) {
                    System.err.println("[DashboardView] Error loading data: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        lblMedicamentsCount.setText("Erreur");
                        lblAlertesCount.setText("Erreur");
                        lblChiffreAffaires.setText("Erreur");
                        lblVentesCount.setText("Erreur");
                        alertTableModel.setRowCount(0);
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private void updateAlertTable() {
        alertTableModel.setRowCount(0);
        if (alerteMedicaments == null) return;

        for (Medicament m : alerteMedicaments) {
            String statut = "";
            if (DateUtils.isDateExpired(m.getDateExpiration())) {
                statut = "Expir\u00e9";
            } else if (DateUtils.isExpirationProche(m.getDateExpiration(), 30)) {
                statut = "Expiration proche";
            } else if (m.getQuantiteStock() <= m.getSeuilAlerte()) {
                statut = "Stock faible";
            }

            String categorieNom = m.getCategorieNom() != null ? m.getCategorieNom() : "";
            String dateExpStr = m.getDateExpiration() != null
                    ? DateUtils.formatDate(m.getDateExpiration()) : "";

            alertTableModel.addRow(new Object[]{
                    m.getNom(),
                    categorieNom,
                    m.getQuantiteStock(),
                    m.getSeuilAlerte(),
                    dateExpStr,
                    statut
            });
        }

        // Color rows based on status
        alertTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String statut = (String) alertTableModel.getValueAt(row, 5);
                    if (null == statut) {
                        c.setBackground(UIManager.getColor("Table.background"));
                        c.setForeground(UIManager.getColor("Table.foreground"));
                    } else switch (statut) {
                        case "Expir\u00e9" -> {
                            c.setBackground(ROW_RED);
                            c.setForeground(ROW_RED_DARK);
                        }
                        case "Stock faible" -> {
                            c.setBackground(ROW_ORANGE);
                            c.setForeground(ROW_ORANGE_DARK);
                        }
                        default -> {
                            c.setBackground(UIManager.getColor("Table.background"));
                            c.setForeground(UIManager.getColor("Table.foreground"));
                        }
                    }
                }
                setHorizontalAlignment(column == 2 || column == 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });
    }
}
