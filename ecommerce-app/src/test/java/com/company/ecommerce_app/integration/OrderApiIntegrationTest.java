package com.company.ecommerce_app.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests: controller, service, repository and the real PostgreSQL database together.
 * Each test creates its own sample data, and everything is rolled back afterwards
 * (that is what @Transactional does in a test), so your database stays clean.
 */
@Tag("integration")
@SpringBootTest(properties = "simulation.payment.mode=SUCCESS")
@AutoConfigureMockMvc
@Transactional
class OrderApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProductRepository productRepository;

    private Customer customer;
    private Product product;

    @BeforeEach
    void createSampleData() {
        customer = customerRepository.save(
                new Customer("Integration Tester", "it-" + UUID.randomUUID() + "@example.test"));
        product = productRepository.save(
                new Product("Integration Widget", new BigDecimal("100.00"), 5));
    }

    private String json(Long customerId, Long productId, int quantity) throws Exception {
        return objectMapper.writeValueAsString(new OrderRequest(customerId, productId, quantity));
    }

    private String placeOrder(int quantity) throws Exception {
        return mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customer.getId(), product.getId(), quantity)))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void placingAnOrder_confirmsIt_andReducesTheStock() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customer.getId(), product.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        assertEquals(3, productRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    void aPlacedOrder_canBeLookedUpAgain() throws Exception {
        String body = placeOrder(1);
        long orderId = objectMapper.readTree(body).get("orderId").asLong();

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void theCustomerProfile_showsTheOrderHistory() throws Exception {
        placeOrder(1);

        mockMvc.perform(get("/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1))
                .andExpect(jsonPath("$.orders[0].status").value("CONFIRMED"));
    }

    @Test
    void ordersForTooManyItems_failBecauseOfStock() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customer.getId(), product.getId(), 6)))   // only 5 in stock
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(OrderService.OUT_OF_STOCK_MESSAGE));
    }

    @Test
    void anUnknownProduct_gives404() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customer.getId(), 999999999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void aQuantityOfZero_gives400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(customer.getId(), product.getId(), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("quantity")));
    }
}
