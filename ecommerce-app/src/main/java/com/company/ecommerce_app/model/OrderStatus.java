package com.company.ecommerce_app.model;

public enum OrderStatus {
    PENDING,    // being processed
    CONFIRMED,  // stock reserved and payment succeeded
    FAILED      // something went wrong and was handled
}
