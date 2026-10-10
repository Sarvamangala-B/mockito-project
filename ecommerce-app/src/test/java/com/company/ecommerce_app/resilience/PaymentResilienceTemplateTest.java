package com.company.ecommerce_app.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.company.ecommerce_app.client.InventoryClient;
import com.company.ecommerce_app.client.PaymentClient;
import com.company.ecommerce_app.client.PaymentResult;
import com.company.ecommerce_app.client.PaymentTimeoutException;
import com.company.ecommerce_app.dto.OrderRequest;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderStatus;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.OrderRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import com.company.ecommerce_app.service.OrderService;
import com.company.ecommerce_app.support.TestData;
import com.company.resilienttesting.failure.FailureScenarios;
import com.company.resilienttesting.failure.SimulatedTimeoutException;
import com.company.resilienttesting.scenario.ResilienceTestTemplate;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Uses the library's ready-made template. The three standard scenarios (timeout, server error,
 * healthy) come from the template; this class only says what the payment dependency is and
 * what "handled gracefully" means for this app. The template's @Tag("resilience") is inherited.
 */
@ExtendWith(MockitoExtension.class)
class PaymentResilienceTemplateTest extends ResilienceTestTemplate<Order> {

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

    private final OrderRequest request = new OrderRequest(1L, 7L, 2);

    private void stubEverythingExceptPayment() {
        FailureScenarios.stubReturns(() -> customerRepository.findById(1L), Optional.of(TestData.customer(1L)));
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(TestData.product(7L, 50)));
        FailureScenarios.stubReturns(() -> inventoryClient.isInStock(7L, 2), true);
    }

    @Override
    protected void stubDependencySucceeds() {
        stubEverythingExceptPayment();
        FailureScenarios.stubReturns(() -> paymentClient.charge(anyString(), any()), PaymentResult.success());
    }

    @Override
    protected void stubDependencyFails(RuntimeException error) {
        stubEverythingExceptPayment();
        // The library's generic "timeout" is translated into this app's own timeout error
        RuntimeException failure = (error instanceof SimulatedTimeoutException)
                ? new PaymentTimeoutException()
                : error;
        FailureScenarios.stubThrows(() -> paymentClient.charge(anyString(), any()), failure);
    }

    @Override
    protected Order callUnderTest() {
        return orderService.placeOrder(request);
    }

    @Override
    protected void assertHandledGracefully(Order order) {
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(OrderService.PAYMENT_UNAVAILABLE_MESSAGE, order.getFailureReason());
    }
}
