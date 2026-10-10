package com.company.ecommerce_app.dto;

/** The shape of every error we return. */
public record ErrorResponse(int status, String message) {
}
