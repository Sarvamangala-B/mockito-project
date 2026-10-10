package com.company.ecommerce_app.support;

import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Product;
import java.math.BigDecimal;
import org.springframework.test.util.ReflectionTestUtils;

/** Ready-made sample objects for the tests (this is the "your own reusable fixture" layer). */
public final class TestData {

    private TestData() {
        // helper class: not meant to be created
    }

    public static Customer customer(Long id) {
        Customer customer = new Customer("Asha Verma", "asha@example.com");
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    /** A product priced at 799.00 with the given stock. */
    public static Product product(Long id, int stock) {
        Product product = new Product("Wireless Mouse", new BigDecimal("799.00"), stock);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
