package com.pharmacie.utils;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

/** Small reusable helpers for clear form validation feedback. */
public final class FormValidation {

    private static final String ORIGINAL_BORDER = "pharmacie.validation.originalBorder";
    private static final String BORDER_SAVED = "pharmacie.validation.borderSaved";

    private FormValidation() { }

    public static void clear(JComponent... fields) {
        for (JComponent field : fields) {
            if (field == null) continue;
            if (Boolean.TRUE.equals(field.getClientProperty(BORDER_SAVED))) {
                field.setBorder((javax.swing.border.Border) field.getClientProperty(ORIGINAL_BORDER));
                field.putClientProperty(ORIGINAL_BORDER, null);
                field.putClientProperty(BORDER_SAVED, null);
            }
            field.setToolTipText(null);
        }
    }

    public static boolean invalid(JComponent field, String message) {
        if (field != null) {
            if (!Boolean.TRUE.equals(field.getClientProperty(BORDER_SAVED))) {
                field.putClientProperty(ORIGINAL_BORDER, field.getBorder());
                field.putClientProperty(BORDER_SAVED, Boolean.TRUE);
            }
            field.setBorder(BorderFactory.createLineBorder(new Color(205, 55, 55), 2));
            field.setToolTipText(message);
            field.requestFocusInWindow();
        }
        DialogUtils.showWarning(message);
        return false;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return true;
        return email.length() <= 100
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true;
        if (phone.length() > 20 || !phone.matches("[+0-9() .-]+")) return false;
        long digits = phone.chars().filter(Character::isDigit).count();
        return digits >= 7 && digits <= 15;
    }
}
