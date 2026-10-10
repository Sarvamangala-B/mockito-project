package com.company.ecommerce_app;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starts the whole application against the real database.
 * Tagged "integration", so it runs in the CI build only (mvn verify -P ci).
 */
@Tag("integration")
@SpringBootTest
class EcommerceAppApplicationTests {

    @Test
    void contextLoads() {
    }
}
