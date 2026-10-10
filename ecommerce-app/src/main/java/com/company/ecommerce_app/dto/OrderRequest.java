package com.company.ecommerce_app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** What the customer sends to place an order. */
public record OrderRequest(
        @NotNull(message = "is required") Long customerId,
        @NotNull(message = "is required") Long productId,
        @Min(value = 1, message = "must be at least 1") int quantity) {
}
