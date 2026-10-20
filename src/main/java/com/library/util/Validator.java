package com.library.util;

import com.library.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Central place for all input validation rules.
 * The class only contains static helpers, so it cannot be instantiated.
 */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern ISBN = Pattern.compile("^(\\d{9}[\\dX]|\\d{13})$");

    private Validator() {
    }

    public static void requireNonBlank(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " cannot be empty.");
        }
    }

    public static void requireMaxLength(String value, int max, String field) throws ValidationException {
        if (value != null && value.length() > max) {
            throw new ValidationException(field + " cannot be longer than " + max + " characters.");
        }
    }

    public static void requireRange(int value, int min, int max, String field) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
    }

    public static void requireEmail(String email) throws ValidationException {
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid e-mail address (example: name@example.com).");
        }
    }

    public static void requirePhone(String phone) throws ValidationException {
        if (!isValidPhone(phone)) {
            throw new ValidationException("Phone number must contain 7-15 digits, optionally starting with '+'.");
        }
    }

    /** Accepts ISBN-10 or ISBN-13; hyphens and spaces are ignored. */
    public static void requireIsbn(String isbn) throws ValidationException {
        if (!isValidIsbn(isbn)) {
            throw new ValidationException("ISBN must have 10 or 13 digits (hyphens are allowed).");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }

    public static boolean isValidIsbn(String isbn) {
        return isbn != null && ISBN.matcher(normalizeIsbn(isbn)).matches();
    }

    public static String normalizeIsbn(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[\\s-]", "").toUpperCase();
    }
}
