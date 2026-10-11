package com.company.ecommerce_app.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderItem;
import com.company.ecommerce_app.model.OrderStatus;
import com.company.ecommerce_app.support.TestData;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** The order and its items, with no database involved. */
class OrderEntityTest {

    private final Customer customer = TestData.customer(1L);
    private final Order order = new Order(customer, new BigDecimal("1598.00"));

    @Test
    void aNewOrder_startsAsPending_withATimestamp() {
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNull(order.getFailureReason());
        assertTrue(order.getItems().isEmpty());
        assertSame(customer, order.getCustomer());
        assertEquals(new BigDecimal("1598.00"), order.getTotal());
    }

    @Test
    void addItem_linksTheItemAndTheOrderBothWays() {
        OrderItem item = new OrderItem(TestData.product(7L, 50), 2, new BigDecimal("799.00"));

        order.addItem(item);

        assertEquals(1, order.getItems().size());
        assertSame(order, item.getOrder());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("799.00"), item.getUnitPrice());
        assertEquals(Long.valueOf(7L), item.getProduct().getId());
    }

    @Test
    void aFailedOrder_remembersWhy() {
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason("Payment was declined.");

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals("Payment was declined.", order.getFailureReason());
    }
}
