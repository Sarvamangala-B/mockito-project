package com.company.ecommerce_app.dto;

import com.company.ecommerce_app.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** A customer with a summary of their orders. */
public record CustomerResponse(Long id, String name, String email, List<OrderSummary> orders) {

    public record OrderSummary(Long orderId, OrderStatus status, BigDecimal total, Instant createdAt) {
    }
}
