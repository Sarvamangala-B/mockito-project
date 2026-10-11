package com.company.ecommerce_app.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** The product endpoints, through the real application and the real database (rolled back afterwards). */
@Tag("integration")
@SpringBootTest(properties = "simulation.payment.mode=SUCCESS")
@AutoConfigureMockMvc
@Transactional
class ProductApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void createProduct() {
        product = productRepository.save(
                new Product("Catalog Widget " + UUID.randomUUID(), new BigDecimal("100.00"), 5));
    }

    @Test
    void theProductList_includesANewProduct() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + product.getId() + ")]").exists());
    }

    @Test
    void aSingleProduct_canBeLookedUp() throws Exception {
        mockMvc.perform(get("/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(product.getName()))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    void anUnknownProduct_gives404() throws Exception {
        mockMvc.perform(get("/products/{id}", 999999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void aNonNumericId_gives400() throws Exception {
        mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("id")));
    }
}
