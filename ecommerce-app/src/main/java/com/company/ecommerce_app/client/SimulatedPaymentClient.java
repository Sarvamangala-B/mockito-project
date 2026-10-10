package com.company.ecommerce_app.client;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A stand-in for a real payment service, so the app can run on its own.
 * The behaviour is chosen in application.properties:
 *   simulation.payment.mode = SUCCESS | DECLINED | TIMEOUT
 * In TIMEOUT mode it waits payment.timeout-ms, then reports a timeout.
 */
@Component
public class SimulatedPaymentClient implements PaymentClient {

    private final String mode;
    private final long timeoutMs;

    public SimulatedPaymentClient(
            @Value("${simulation.payment.mode:SUCCESS}") String mode,
            @Value("${payment.timeout-ms:2000}") long timeoutMs) {
        this.mode = mode;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public PaymentResult charge(String orderRef, BigDecimal amount) {
        if ("TIMEOUT".equalsIgnoreCase(mode)) {
            try {
                Thread.sleep(timeoutMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new PaymentTimeoutException();
        }
        if ("DECLINED".equalsIgnoreCase(mode)) {
            return PaymentResult.declined("Payment was declined.");
        }
        return PaymentResult.success();
    }
}
