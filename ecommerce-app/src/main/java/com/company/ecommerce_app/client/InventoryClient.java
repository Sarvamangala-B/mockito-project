package com.company.ecommerce_app.client;

/** The stock-checking service, described as an interface so tests can replace it with a fake. */
public interface InventoryClient {
    boolean isInStock(Long productId, int quantity);
}
