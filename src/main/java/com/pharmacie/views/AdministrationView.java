package com.pharmacie.views;

import com.pharmacie.models.Utilisateur;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/** Central administration workspace for user and database administration. */
public class AdministrationView extends JPanel {

    public AdministrationView(MainFrame mainFrame) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));

        Utilisateur admin = mainFrame.getUtilisateur();
        JLabel title = new JLabel("Espace Administration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Gestion des accès et protection des données — "
                + admin.getNomComplet());
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Utilisateurs", new UtilisateurView(mainFrame));
        tabs.addTab("Sauvegarde / Restauration", new BackupView(mainFrame));

        add(heading, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
