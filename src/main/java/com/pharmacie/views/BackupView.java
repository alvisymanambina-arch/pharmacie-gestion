package com.pharmacie.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.pharmacie.utils.BackupUtils;
import com.pharmacie.utils.DialogUtils;

public class BackupView extends JPanel {

   

    private JLabel lblBackupFile;
    private JLabel lblRestoreFile;
    private JCheckBox chkConfirm;
    private JButton btnExporter;
    private JButton btnRestaurer;
    private JProgressBar progressBar;

    public BackupView(MainFrame mainFrame) {
        
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        lblBackupFile = new JLabel("Fichier : " + BackupUtils.generateBackupFileName());
        lblRestoreFile = new JLabel("Aucun fichier sélectionné");
        chkConfirm = new JCheckBox(
                "Je comprends que la restauration va écraser les données actuelles");
        btnExporter = new JButton("📁 Choisir l'emplacement et exporter");
        btnRestaurer = new JButton("📂 Sélectionner un fichier et restaurer");
        btnRestaurer.setEnabled(false);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Sauvegarde et Restauration");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel lblSubtitle = new JLabel(
                "Gérez les sauvegardes de votre base de données PostgreSQL / Supabase");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSubtitle.setForeground(Color.GRAY);

        JPanel panelTitle = new JPanel(new BorderLayout());
        panelTitle.add(lblTitle, BorderLayout.NORTH);
        panelTitle.add(lblSubtitle, BorderLayout.CENTER);

        // Panel sauvegarde (gauche)
        JPanel panelBackup = new JPanel(new BorderLayout(10, 10));
        panelBackup.setBorder(BorderFactory.createTitledBorder("💾 Sauvegarde"));
        panelBackup.setPreferredSize(new Dimension(500, 200));

        JTextArea txtBackupDesc = new JTextArea(
                """
                La sauvegarde exporte l'int\u00e9gralit\u00e9 de la base de donn\u00e9es 'pharmacie_db' dans un fichier SQL.
                
                Ce fichier pourra \u00eatre utilis\u00e9 pour restaurer la base en cas de probl\u00e8me.""");
        txtBackupDesc.setWrapStyleWord(true);
        txtBackupDesc.setLineWrap(true);
        txtBackupDesc.setEditable(false);
        txtBackupDesc.setBackground(panelBackup.getBackground());
        txtBackupDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panelBackup.add(txtBackupDesc, BorderLayout.NORTH);
        panelBackup.add(lblBackupFile, BorderLayout.CENTER);

        btnExporter.setBackground(new Color(76, 175, 80));
        btnExporter.setForeground(Color.WHITE);
        btnExporter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExporter.setFocusPainted(false);
        btnExporter.addActionListener(e -> exporter());
        panelBackup.add(btnExporter, BorderLayout.SOUTH);

        // Panel restauration (droite)
        JPanel panelRestore = new JPanel(new BorderLayout(10, 10));
        panelRestore.setBorder(BorderFactory.createTitledBorder("♻️ Restauration"));
        panelRestore.setPreferredSize(new Dimension(500, 200));

        JTextArea txtRestoreDesc = new JTextArea(
                """
                La restauration remplace les donn\u00e9es actuelles par celles contenues dans un fichier SQL de sauvegarde.
                
                \u26a0\ufe0f ATTENTION : cette op\u00e9ration est irr\u00e9versible !""");
        txtRestoreDesc.setWrapStyleWord(true);
        txtRestoreDesc.setLineWrap(true);
        txtRestoreDesc.setEditable(false);
        txtRestoreDesc.setBackground(panelRestore.getBackground());
        txtRestoreDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnChoisirFichier = new JButton("Choisir le fichier...");
        btnChoisirFichier.addActionListener(e -> choisirFichier());

        panelRestore.add(txtRestoreDesc, BorderLayout.NORTH);
        panelRestore.add(lblRestoreFile, BorderLayout.CENTER);

        JPanel panelRestoreBtn = new JPanel(new BorderLayout());
        panelRestoreBtn.add(btnChoisirFichier, BorderLayout.NORTH);
        panelRestoreBtn.add(chkConfirm, BorderLayout.CENTER);

        btnRestaurer.setBackground(new Color(244, 67, 54));
        btnRestaurer.setForeground(Color.WHITE);
        btnRestaurer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRestaurer.setFocusPainted(false);
        btnRestaurer.addActionListener(e -> restaurer());

        chkConfirm.addActionListener(e ->
                btnRestaurer.setEnabled(chkConfirm.isSelected()
                        && !lblRestoreFile.getText().equals("Aucun fichier sélectionné")));

        panelRestoreBtn.add(btnRestaurer, BorderLayout.SOUTH);
        panelRestore.add(panelRestoreBtn, BorderLayout.SOUTH);

        // Panel bas avec la barre de progression
        JPanel panelBottom = new JPanel(new BorderLayout());
        progressBar.setPreferredSize(new Dimension(0, 25));
        panelBottom.add(progressBar, BorderLayout.CENTER);

        // Assemblage
        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 15, 15));
        panelCenter.add(panelBackup);
        panelCenter.add(panelRestore);

        add(panelTitle, BorderLayout.NORTH);
        add(panelCenter, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void exporter() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer la sauvegarde");
        chooser.setSelectedFile(new File(BackupUtils.generateBackupFileName()));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers SQL (*.sql)", "sql"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String filePath = chooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".sql")) {
            filePath += ".sql";
        }
        final String finalPath = filePath;

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        btnExporter.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return BackupUtils.exportDatabase(finalPath);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                btnExporter.setEnabled(true);
                try {
                    if (get()) {
                        DialogUtils.showInformation(
                                "Sauvegarde réussie !\nFichier : " + finalPath
                                + "\n\nNote : PostgreSQL doit être installé et le binaire "
                                + "'pg_dump' doit être accessible dans le PATH.");
                    } else {
                        DialogUtils.showError("""
                                              \u00c9chec de la sauvegarde.
                                              V\u00e9rifiez que PostgreSQL est install\u00e9 et que 'pg_dump' est accessible depuis la ligne de commande.""");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur lors de la sauvegarde");
                }
            }
        };
        worker.execute();
    }

    private void choisirFichier() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sélectionner le fichier de sauvegarde");
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers SQL (*.sql)", "sql"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            lblRestoreFile.setText("Fichier : " + selected.getAbsolutePath());
            btnRestaurer.setEnabled(chkConfirm.isSelected());
        }
    }

    private void restaurer() {
        String fileText = lblRestoreFile.getText();
        if (!fileText.startsWith("Fichier : ")) {
            DialogUtils.showWarning("Veuillez d'abord sélectionner un fichier.");
            return;
        }
        if (!chkConfirm.isSelected()) {
            DialogUtils.showWarning("Veuillez cocher la case de confirmation.");
            return;
        }
        if (!DialogUtils.showConfirmation("""
                                          \u26a0\ufe0f ATTENTION \u26a0\ufe0f
                                          
                                          Cette op\u00e9ration va REMPLACER toutes les donn\u00e9es actuelles.
                                          Voulez-vous vraiment continuer ?""")) {
            return;
        }

        final String filePath = fileText.substring("Fichier : ".length());

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        btnRestaurer.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return BackupUtils.importDatabase(filePath);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                btnRestaurer.setEnabled(true);
                try {
                    if (get()) {
                        DialogUtils.showInformation("""
                                                    Restauration r\u00e9ussie !
                                                    Veuillez red\u00e9marrer l'application.""");
                    } else {
                        DialogUtils.showError("""
                                              \u00c9chec de la restauration.
                                              V\u00e9rifiez la console pour plus de d\u00e9tails.""");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    DialogUtils.showErrorMessage(e, "Erreur lors de la restauration");
                }
            }
        };
        worker.execute();
    }
}
