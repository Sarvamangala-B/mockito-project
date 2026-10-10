package com.company.ecommerce_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderStatus;

/** What we send back about an order. "message" only appears when there is one. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(Long orderId, OrderStatus status, String message) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getStatus(), order.getFailureReason());
    }
}
