package com.company.ecommerce_app.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.client.PaymentResult;
import com.company.ecommerce_app.client.PaymentTimeoutException;
import com.company.ecommerce_app.client.SimulatedPaymentClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** The stand-in payment service behaves as its "mode" setting says. */
class SimulatedPaymentClientTest {

    private static final BigDecimal AMOUNT = new BigDecimal("10.00");

    @Test
    void successMode_approvesThePayment() {
        PaymentResult result = new SimulatedPaymentClient("SUCCESS", 1).charge("ORDER-1", AMOUNT);

        assertTrue(result.successful());
    }

    @Test
    void declinedMode_declinesThePayment() {
        PaymentResult result = new SimulatedPaymentClient("DECLINED", 1).charge("ORDER-1", AMOUNT);

        assertFalse(result.successful());
        assertEquals("Payment was declined.", result.message());
    }

    @Test
    void timeoutMode_reportsATimeout() {
        SimulatedPaymentClient client = new SimulatedPaymentClient("TIMEOUT", 1);

        assertThrows(PaymentTimeoutException.class, () -> client.charge("ORDER-1", AMOUNT));
    }

    @Test
    void modeNames_areNotCaseSensitive() {
        PaymentResult result = new SimulatedPaymentClient("declined", 1).charge("ORDER-1", AMOUNT);

        assertFalse(result.successful());
    }

    @Test
    void anUnknownMode_behavesLikeSuccess() {
        PaymentResult result = new SimulatedPaymentClient("SOMETHING-ELSE", 1).charge("ORDER-1", AMOUNT);

        assertTrue(result.successful());
    }
}
