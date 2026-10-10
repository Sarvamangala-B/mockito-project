package com.company.ecommerce_app.client;

/** Thrown when the payment service does not answer in time. */
public class PaymentTimeoutException extends RuntimeException {

    public PaymentTimeoutException() {
        super("The payment service did not respond in time.");
    }

    public PaymentTimeoutException(String message) {
        super(message);
    }
}
