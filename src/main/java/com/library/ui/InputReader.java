package com.library.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Reads and validates everything the user types.
 * Each method keeps asking until a correctly formatted value is entered,
 * so the rest of the program never has to deal with bad console input.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Reads one raw line (trimmed). */
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            error("This field cannot be empty.");
        }
    }

    /** Reads text that must satisfy a rule; {@code message} explains the rule. */
    public String readMatching(String prompt, Predicate<String> rule, String message) {
        while (true) {
            String value = readRequired(prompt);
            if (rule.test(value)) {
                return value;
            }
            error(message);
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                error("'" + value + "' is not a whole number. Please try again.");
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            error("Please enter a number between " + min + " and " + max + ".");
        }
    }

    /** Reads a positive record ID. */
    public int readId(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                error("Invalid date. Use the format YYYY-MM-DD, e.g. " + LocalDate.now() + ".");
            }
        }
    }

    // ----- "edit" variants: pressing Enter keeps the current value -----

    public String readOptional(String label, String current) {
        String value = readLine(label + " [" + current + "]: ");
        return value.isEmpty() ? current : value;
    }

    public int readOptionalInt(String label, int current, int min, int max) {
        while (true) {
            String value = readLine(label + " [" + current + "]: ");
            if (value.isEmpty()) {
                return current;
            }
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                error("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                error("'" + value + "' is not a whole number. Please try again.");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String label, Class<E> type, E current) {
        E[] values = type.getEnumConstants();
        for (int i = 0; i < values.length; i++) {
            System.out.println("   " + (i + 1) + ". " + values[i]);
        }
        String suffix = current == null ? ": " : " [" + current + "]: ";
        while (true) {
            String value = readLine(label + " (1-" + values.length + ")" + suffix);
            if (value.isEmpty() && current != null) {
                return current;
            }
            try {
                int index = Integer.parseInt(value);
                if (index >= 1 && index <= values.length) {
                    return values[index - 1];
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error message below
            }
            error("Please choose a number from 1 to " + values.length + ".");
        }
    }

    public boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (y/n): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            error("Please answer 'y' or 'n'.");
        }
    }

    public void pause() {
        readLine("\nPress Enter to continue...");
    }

    private static void error(String message) {
        System.out.println("  [!] " + message);
    }
}
