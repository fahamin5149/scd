package com.library.ui;

/** Signals that standard input was closed (e.g. Ctrl+D / Ctrl+Z), so the program should end. */
public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Input stream closed.");
    }
}
