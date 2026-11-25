package com.pharmacie.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/** A password field with a small button to show or hide its contents. */
final class PasswordFieldPanel extends JPanel {

    private final JPasswordField passwordField;
    private final JButton toggleButton;
    private final EyeIcon eyeIcon = new EyeIcon();
    private final char hiddenEchoChar;
    private boolean passwordVisible;

    PasswordFieldPanel(JPasswordField passwordField) {
        super(new BorderLayout(4, 0));
        this.passwordField = passwordField;
        this.hiddenEchoChar = passwordField.getEchoChar();

        toggleButton = new JButton(eyeIcon);
        toggleButton.setToolTipText("Afficher le mot de passe");
        toggleButton.getAccessibleContext().setAccessibleName("Afficher le mot de passe");
        toggleButton.setFocusable(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setMargin(new Insets(0, 4, 0, 4));
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.addActionListener(e -> togglePasswordVisibility());

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        add(passwordField, BorderLayout.CENTER);
        add(toggleButton, BorderLayout.EAST);
    }

    JPasswordField getPasswordField() {
        return passwordField;
    }

    void setPasswordEnabled(boolean enabled) {
        passwordField.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? '\0' : hiddenEchoChar);
        String action = passwordVisible ? "Masquer" : "Afficher";
        toggleButton.setToolTipText(action + " le mot de passe");
        toggleButton.getAccessibleContext().setAccessibleName(action + " le mot de passe");
        eyeIcon.setVisible(passwordVisible);
        toggleButton.repaint();
        passwordField.requestFocusInWindow();
    }

    private static final class EyeIcon implements Icon {
        private boolean visible;

        void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.translate(x, y);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(component.isEnabled() ? new Color(95, 105, 115) : Color.GRAY);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D eye = new Path2D.Double();
            eye.moveTo(1.5, 9);
            eye.quadTo(9, 1, 16.5, 9);
            eye.quadTo(9, 17, 1.5, 9);
            g2.draw(eye);
            g2.draw(new Ellipse2D.Double(6.1, 6.1, 5.8, 5.8));

            if (!visible) {
                g2.drawLine(3, 15, 15, 3);
            }
            g2.dispose();
        }
    }
}
