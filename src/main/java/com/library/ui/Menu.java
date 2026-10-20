package com.library.ui;

import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract, reusable menu screen (Template Method pattern).
 *
 * <p>The {@link #show()} loop - print options, read a choice, run it, repeat -
 * is written only once here. Subclasses just register their options with
 * {@link #option(String, Action)}.</p>
 */
public abstract class Menu {

    /** Something that happens when a menu entry is chosen. */
    @FunctionalInterface
    protected interface Action {
        void run() throws LibraryException;
    }

    /** {@code screen} is true for actions that print a result the user should read before the menu returns. */
    private record Entry(String label, Action action, boolean screen) {
    }

    protected final InputReader in;
    private final String title;
    private final String exitLabel;
    private final Map<Integer, Entry> entries = new LinkedHashMap<>();

    protected Menu(InputReader in, String title, String exitLabel) {
        this.in = in;
        this.title = title;
        this.exitLabel = exitLabel;
    }

    protected void option(String label, Action action) {
        entries.put(entries.size() + 1, new Entry(label, action, true));
    }

    /** Adds an entry that opens another menu (no heading or pause around it). */
    protected void submenu(String label, Menu menu) {
        entries.put(entries.size() + 1, new Entry(label, menu::show, false));
    }

    /** Displays the menu repeatedly until the user picks 0. */
    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = in.readInt("Enter your choice: ");
            if (choice == 0) {
                running = false;
            } else if (entries.containsKey(choice)) {
                Entry entry = entries.get(choice);
                if (entry.screen()) {
                    ConsoleUtil.heading(entry.label());
                }
                runSafely(entry.action());
                if (entry.screen()) {
                    in.pause();
                }
            } else {
                System.out.println("  [!] Invalid choice '" + choice + "'. Please select one of the listed options.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        ConsoleUtil.banner(title);
        entries.forEach((number, entry) -> System.out.printf("  %d. %s%n", number, entry.label()));
        System.out.printf("  0. %s%n", exitLabel);
        System.out.println(ConsoleUtil.LINE);
    }

    /** Runs an action and turns any expected error into a friendly message instead of a crash. */
    private static void runSafely(Action action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            System.out.println("  [DATABASE ERROR] " + e.getMessage());
        } catch (LibraryException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }
}
