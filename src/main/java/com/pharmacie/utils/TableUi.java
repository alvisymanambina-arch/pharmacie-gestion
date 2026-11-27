package com.pharmacie.utils;

import java.awt.Font;
import java.awt.Dimension;

import javax.swing.JTable;

/** Shared, unobtrusive styling for the application's data tables. */
public final class TableUi {

    private TableUi() { }

    public static void apply(JTable table) {
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
    }
}
