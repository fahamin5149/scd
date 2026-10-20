package com.library.ui;

/** Small helpers for consistent console formatting. */
public final class ConsoleUtil {

    public static final int WIDTH = 56;
    public static final String LINE = "=".repeat(WIDTH);

    private ConsoleUtil() {
    }

    public static void banner(String title) {
        System.out.println(LINE);
        int padding = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println(LINE);
    }

    public static void heading(String text) {
        System.out.println();
        System.out.println("--- " + text + " " + "-".repeat(Math.max(3, WIDTH - text.length() - 5)));
    }

    public static void success(String message) {
        System.out.println("  [OK] " + message);
    }

    public static void info(String message) {
        System.out.println("  " + message);
    }
}
