package com.library.model;

import com.library.exception.ValidationException;
import com.library.util.Validator;

/**
 * Abstract description of a person known to the library.
 * Common contact data lives here; concrete subclasses add role specific data.
 */
public abstract class Person extends Entity {

    private String name;
    private String email;
    private String phone;

    protected Person() {
    }

    protected Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Short human readable description of the person's role (implemented by subclasses). */
    public abstract String getRole();

    @Override
    public void validate() throws ValidationException {
        Validator.requireNonBlank(name, "Name");
        Validator.requireMaxLength(name, 60, "Name");
        Validator.requireEmail(email);
        Validator.requirePhone(phone);
    }
}
