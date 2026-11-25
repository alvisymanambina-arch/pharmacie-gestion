package com.pharmacie.utils;

import com.pharmacie.config.AppConstants;

import javax.swing.JOptionPane;

public class DialogUtils {

    private DialogUtils() {
    }

    public static void showInformation(String message) {
        JOptionPane.showMessageDialog(null, message,
                AppConstants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Attention", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirmation(String message) {
        int result = JOptionPane.showConfirmDialog(null, message,
                "Confirmation", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static String showInput(String message) {
        return JOptionPane.showInputDialog(null, message,
                AppConstants.APP_NAME, JOptionPane.QUESTION_MESSAGE);
    }

    public static String showInput(String message, String defaultValue) {
        Object result = JOptionPane.showInputDialog(null, message,
                AppConstants.APP_NAME, JOptionPane.QUESTION_MESSAGE,
                null, null, defaultValue);
        return result != null ? result.toString() : null;
    }

    public static void showErrorMessage(Exception e, String context) {
        String message = context + "\nErreur : " + e.getMessage();
        JOptionPane.showMessageDialog(null, message,
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
