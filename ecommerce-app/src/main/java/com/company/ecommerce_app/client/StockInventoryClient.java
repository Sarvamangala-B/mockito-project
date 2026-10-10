package com.company.ecommerce_app.client;

import com.company.ecommerce_app.repository.ProductRepository;
import org.springframework.stereotype.Component;

/** Checks stock using the products table in our own database. */
@Component
public class StockInventoryClient implements InventoryClient {

    private final ProductRepository productRepository;

    public StockInventoryClient(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isInStock(Long productId, int quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getStock() >= quantity)
                .orElse(false);
    }
}
