package com.company.ecommerce_app.service;

import com.company.ecommerce_app.exception.NotFoundException;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " was not found."));
    }
}
