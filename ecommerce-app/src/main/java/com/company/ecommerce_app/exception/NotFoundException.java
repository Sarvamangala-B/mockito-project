package com.company.ecommerce_app.exception;

/** Thrown when something the caller asked for does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
