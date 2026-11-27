package com.pharmacie.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateUtils {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateUtils() {}

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr,
                    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr,
                        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr,
                    DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr,
                        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    public static boolean isDateExpired(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isExpirationProche(LocalDate date, int jours) {
        LocalDate today = LocalDate.now();
        return date != null && !date.isBefore(today)
                && !date.isAfter(today.plusDays(jours));
    }

    public static String getMoisNom(int mois) {
        String[] moisNoms = {
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };
        if (mois >= 1 && mois <= 12) {
            return moisNoms[mois - 1];
        }
        return "";
    }
}
