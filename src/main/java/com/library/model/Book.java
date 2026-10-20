package com.library.model;

import com.library.exception.ValidationException;
import com.library.util.Validator;

import java.time.Year;

/** A book title held by the library, possibly with several physical copies. */
public class Book extends Entity {

    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    public Book() {
    }

    public Book(String isbn, String title, String author, String genre, int publicationYear, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    /** Number of copies that are currently lent out. */
    public int getIssuedCopies() {
        return totalCopies - availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.requireIsbn(isbn);
        Validator.requireNonBlank(title, "Title");
        Validator.requireMaxLength(title, 100, "Title");
        Validator.requireNonBlank(author, "Author");
        Validator.requireMaxLength(author, 60, "Author");
        Validator.requireNonBlank(genre, "Genre");
        Validator.requireMaxLength(genre, 30, "Genre");
        Validator.requireRange(publicationYear, 1450, Year.now().getValue(), "Publication year");
        Validator.requireRange(totalCopies, 1, 1000, "Total copies");
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new ValidationException("Available copies must be between 0 and the total number of copies.");
        }
    }

    @Override
    public String toString() {
        return "Book #" + getId() + " - \"" + title + "\" by " + author;
    }
}
