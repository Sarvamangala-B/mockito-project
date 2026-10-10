package com.company.ecommerce_app.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.ecommerce_app.service.OrderService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Unit test: one small piece of logic on its own, with no mocks and no database. */
class OrderTotalCalculationTest {

    @Test
    void total_isUnitPriceTimesQuantity() {
        BigDecimal total = OrderService.calculateTotal(new BigDecimal("799.00"), 2);

        assertEquals(new BigDecimal("1598.00"), total);
    }

    @Test
    void total_forOneItem_isTheUnitPrice() {
        BigDecimal total = OrderService.calculateTotal(new BigDecimal("299.00"), 1);

        assertEquals(new BigDecimal("299.00"), total);
    }

    @Test
    void total_keepsPennies() {
        BigDecimal total = OrderService.calculateTotal(new BigDecimal("19.99"), 3);

        assertEquals(new BigDecimal("59.97"), total);
    }
}
