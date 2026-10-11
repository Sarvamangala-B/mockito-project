package com.company.ecommerce_app.mock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.client.StockInventoryClient;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.ecommerce_app.support.TestData;
import com.company.resilienttesting.failure.FailureScenarios;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockInventoryClientTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    StockInventoryClient inventoryClient;

    private final Product product = TestData.product(7L, 5);

    @Test
    void isInStock_whenTheStockCoversTheQuantity() {
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(product));

        assertTrue(inventoryClient.isInStock(7L, 5));
    }

    @Test
    void isNotInStock_whenTheQuantityIsMoreThanTheStock() {
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(product));

        assertFalse(inventoryClient.isInStock(7L, 6));
    }

    @Test
    void isNotInStock_forAnUnknownProduct() {
        FailureScenarios.stubReturns(() -> productRepository.findById(99L), Optional.empty());

        assertFalse(inventoryClient.isInStock(99L, 1));
    }
}
