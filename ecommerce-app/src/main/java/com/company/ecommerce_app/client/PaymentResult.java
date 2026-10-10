package com.company.ecommerce_app.client;

/** The answer from the payment service: it either worked or was declined. */
public record PaymentResult(boolean successful, String message) {

    public static PaymentResult success() {
        return new PaymentResult(true, null);
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(false, message);
    }
}
