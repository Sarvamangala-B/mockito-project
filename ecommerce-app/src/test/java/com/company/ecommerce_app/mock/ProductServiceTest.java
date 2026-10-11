package com.company.ecommerce_app.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.exception.NotFoundException;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.ecommerce_app.service.ProductService;
import com.company.ecommerce_app.support.TestData;
import com.company.resilienttesting.failure.FailureScenarios;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void listProducts_returnsEverythingTheRepositoryHolds() {
        List<Product> products = List.of(TestData.product(1L, 10), TestData.product(2L, 20));
        FailureScenarios.stubReturns(() -> productRepository.findAll(), products);

        assertEquals(products, productService.listProducts());
    }

    @Test
    void getProduct_returnsTheProduct() {
        Product product = TestData.product(7L, 5);
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(product));

        assertSame(product, productService.getProduct(7L));
    }

    @Test
    void getProduct_forAnUnknownId_reportsNotFound() {
        FailureScenarios.stubReturns(() -> productRepository.findById(99L), Optional.empty());

        NotFoundException error = assertThrows(NotFoundException.class, () -> productService.getProduct(99L));

        assertTrue(error.getMessage().contains("99"));
    }
}
