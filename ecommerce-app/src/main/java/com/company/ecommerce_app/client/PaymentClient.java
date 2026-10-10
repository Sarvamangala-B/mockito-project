package com.company.ecommerce_app.client;

import java.math.BigDecimal;

/** The outside payment service, described as an interface so tests can replace it with a fake. */
public interface PaymentClient {
    PaymentResult charge(String orderRef, BigDecimal amount);
}
