package com.company.ecommerce_app.config;

import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Adds a little sample data on startup (only if the tables are empty) so you can try the endpoints. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public DataSeeder(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer("Asha Verma", "asha@example.com"));
        }
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Wireless Mouse", new BigDecimal("799.00"), 50));
            productRepository.save(new Product("Mechanical Keyboard", new BigDecimal("2499.00"), 20));
            productRepository.save(new Product("USB-C Cable", new BigDecimal("299.00"), 100));
            productRepository.save(new Product("Limited Edition Mug", new BigDecimal("499.00"), 1));
        }
    }
}
