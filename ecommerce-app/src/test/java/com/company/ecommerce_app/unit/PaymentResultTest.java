package com.company.ecommerce_app.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.client.PaymentResult;
import org.junit.jupiter.api.Test;

class PaymentResultTest {

    @Test
    void success_isSuccessfulAndHasNoMessage() {
        PaymentResult result = PaymentResult.success();

        assertTrue(result.successful());
        assertNull(result.message());
    }

    @Test
    void declined_isNotSuccessfulAndKeepsTheMessage() {
        PaymentResult result = PaymentResult.declined("Card expired");

        assertFalse(result.successful());
        assertEquals("Card expired", result.message());
    }
}
