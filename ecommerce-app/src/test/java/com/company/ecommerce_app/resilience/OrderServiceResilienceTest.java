package com.company.ecommerce_app.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.company.ecommerce_app.client.InventoryClient;
import com.company.ecommerce_app.client.PaymentClient;
import com.company.ecommerce_app.client.PaymentResult;
import com.company.ecommerce_app.client.PaymentTimeoutException;
import com.company.ecommerce_app.dto.OrderRequest;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderStatus;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.OrderRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.ecommerce_app.service.OrderService;
import com.company.ecommerce_app.support.TestData;
import com.company.resilienttesting.failure.FailureScenarios;
import com.company.resilienttesting.failure.SimulatedServerErrorException;
import com.company.resilienttesting.verification.VerifyCalls;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Resilience tests: what happens when the payment service misbehaves.
 * These prove the rules decided in Step 3d: up to 3 payment attempts, a clean FAILED order at the end.
 * Tagged "resilience", so they run in the CI build only.
 */
@Tag("resilience")
@ExtendWith(MockitoExtension.class)
class OrderServiceResilienceTest {

    @Mock
    PaymentClient paymentClient;
    @Mock
    InventoryClient inventoryClient;
    @Mock
    OrderRepository orderRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OrderService orderService;

    private final Customer customer = TestData.customer(1L);
    private final Product product = TestData.product(7L, 50);
    private final OrderRequest request = new OrderRequest(1L, 7L, 2);

    private void stubCustomerProductAndStock() {
        FailureScenarios.stubReturns(() -> customerRepository.findById(1L), Optional.of(customer));
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(product));
        FailureScenarios.stubReturns(() -> inventoryClient.isInStock(7L, 2), true);
    }

    @Test
    void order_fails_cleanly_whenPaymentKeepsTimingOut() {
        stubCustomerProductAndStock();
        FailureScenarios.stubThrows(() -> paymentClient.charge(anyString(), any()),
                new PaymentTimeoutException());

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(OrderService.PAYMENT_UNAVAILABLE_MESSAGE, order.getFailureReason());
        assertEquals(50, product.getStock());   // no stock was taken
        VerifyCalls.calledExactly(paymentClient, 3).charge(anyString(), any());   // tried 3 times
    }

    @Test
    void order_isConfirmed_whenPaymentTimesOutTwiceAndThenSucceeds() {
        stubCustomerProductAndStock();
        FailureScenarios.stubFailsThenReturns(() -> paymentClient.charge(anyString(), any()),
                new PaymentTimeoutException(), 2, PaymentResult.success());

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNull(order.getFailureReason());
        assertEquals(48, product.getStock());
        VerifyCalls.calledExactly(paymentClient, 3).charge(anyString(), any());
    }

    @Test
    void order_fails_cleanly_andIsNotRetried_whenPaymentHasAServerError() {
        stubCustomerProductAndStock();
        FailureScenarios.stubThrows(() -> paymentClient.charge(anyString(), any()),
                new SimulatedServerErrorException(500));

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(OrderService.PAYMENT_UNAVAILABLE_MESSAGE, order.getFailureReason());
        VerifyCalls.calledOnce(paymentClient).charge(anyString(), any());   // server errors are not retried
    }

    @Test
    void order_isStillConfirmed_whenPaymentAnswersSlowlyButWithinLimits() {
        stubCustomerProductAndStock();
        FailureScenarios.stubDelayed(() -> paymentClient.charge(anyString(), any()),
                PaymentResult.success(), Duration.ofMillis(200));

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        VerifyCalls.calledOnce(paymentClient).charge(anyString(), any());
    }
}
