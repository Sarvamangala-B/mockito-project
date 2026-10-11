package com.company.ecommerce_app.mock;

import static org.mockito.ArgumentMatchers.any;

import com.company.ecommerce_app.config.DataSeeder;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.resilienttesting.failure.FailureScenarios;
import com.company.resilienttesting.verification.VerifyCalls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    DataSeeder dataSeeder;

    @Test
    void emptyTables_receiveTheSampleData() {
        FailureScenarios.stubReturns(() -> customerRepository.count(), 0L);
        FailureScenarios.stubReturns(() -> productRepository.count(), 0L);

        dataSeeder.run();

        VerifyCalls.calledOnce(customerRepository).save(any(Customer.class));
        VerifyCalls.calledExactly(productRepository, 4).save(any(Product.class));
    }

    @Test
    void tablesThatAlreadyHoldData_areLeftAlone() {
        FailureScenarios.stubReturns(() -> customerRepository.count(), 3L);
        FailureScenarios.stubReturns(() -> productRepository.count(), 3L);

        dataSeeder.run();

        VerifyCalls.neverCalled(customerRepository).save(any(Customer.class));
        VerifyCalls.neverCalled(productRepository).save(any(Product.class));
    }
}
