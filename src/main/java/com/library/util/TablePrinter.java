package com.library.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Prints rows of data as a neatly aligned text table in the console.
 *
 * <pre>
 * TablePrinter table = new TablePrinter("ID", "Name");
 * table.addRow(1, "Alice");
 * table.print();
 * </pre>
 */
public class TablePrinter {

    private static final int MAX_CELL_WIDTH = 32;

    private final String[] headers;
    private final List<String[]> rows = new ArrayList<>();

    public TablePrinter(String... headers) {
        this.headers = headers;
    }

    public void addRow(Object... values) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Object value = i < values.length ? values[i] : "";
            row[i] = truncate(value == null ? "-" : value.toString());
        }
        rows.add(row);
    }

    public void print() {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildLine(headers, widths));
        System.out.println(separator);
        for (String[] row : rows) {
            System.out.println(buildLine(row, widths));
        }
        System.out.println(separator);
        System.out.println(rows.size() + " record(s) found.");
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append('+');
        }
        return sb.toString();
    }

    private static String buildLine(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            sb.append(' ').append(String.format("%-" + widths[i] + "s", cells[i])).append(" |");
        }
        return sb.toString();
    }

    private static String truncate(String text) {
        return text.length() <= MAX_CELL_WIDTH ? text : text.substring(0, MAX_CELL_WIDTH - 3) + "...";
    }
}
