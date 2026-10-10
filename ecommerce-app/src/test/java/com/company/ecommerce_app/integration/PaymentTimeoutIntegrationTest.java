package com.company.ecommerce_app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.ecommerce_app.dto.OrderRequest;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.ecommerce_app.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * The same timeout rule as the resilience tests, but through the real application wiring:
 * the simulated payment service is switched to TIMEOUT mode (with a very short wait of 10 ms).
 */
@Tag("integration")
@SpringBootTest(properties = {"simulation.payment.mode=TIMEOUT", "payment.timeout-ms=10"})
@AutoConfigureMockMvc
@Transactional
class PaymentTimeoutIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProductRepository productRepository;

    @Test
    void whenPaymentKeepsTimingOut_theOrderFailsCleanly() throws Exception {
        Customer customer = customerRepository.save(
                new Customer("Timeout Tester", "to-" + UUID.randomUUID() + "@example.test"));
        Product product = productRepository.save(
                new Product("Timeout Widget", new BigDecimal("100.00"), 5));
        String body = objectMapper.writeValueAsString(new OrderRequest(customer.getId(), product.getId(), 1));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(OrderService.PAYMENT_UNAVAILABLE_MESSAGE));
    }
}
