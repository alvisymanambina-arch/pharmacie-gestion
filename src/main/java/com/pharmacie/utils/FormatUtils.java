package com.pharmacie.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtils {

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');

        CURRENCY_FORMAT = new DecimalFormat("#,##0.00 Ar", symbols);
        NUMBER_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private FormatUtils() {}

    public static synchronized String formatCurrency(BigDecimal montant) {
        if (montant == null) return "0,00 Ar";
        return CURRENCY_FORMAT.format(montant);
    }

    public static synchronized String formatNumber(int nombre) {
        return NUMBER_FORMAT.format(nombre);
    }

    public static synchronized String formatNumber(Object nombre) {
        if (nombre == null) return "0";
        return NUMBER_FORMAT.format(nombre);
    }

    public static BigDecimal parseCurrency(String montantStr) {
        if (montantStr == null || montantStr.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            String cleaned = montantStr.replaceAll("[^0-9,.-]", "")
                    .replace(" ", "")
                    .replace("\u00A0", "");
            return new BigDecimal(cleaned.replace(",", "."))
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static int parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(str.trim().replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String capitalizeAll(String str) {
        if (str == null || str.isEmpty()) return "";
        String[] words = str.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }
        return sb.toString();
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
