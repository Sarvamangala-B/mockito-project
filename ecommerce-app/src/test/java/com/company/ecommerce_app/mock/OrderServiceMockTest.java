package com.company.ecommerce_app.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.company.ecommerce_app.client.InventoryClient;
import com.company.ecommerce_app.client.PaymentClient;
import com.company.ecommerce_app.client.PaymentResult;
import com.company.ecommerce_app.dto.OrderRequest;
import com.company.ecommerce_app.exception.NotFoundException;
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
import com.company.resilienttesting.verification.VerifyCalls;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock tests: the real OrderService is tested against FAKE payment, stock and database parts.
 * The fakes are told how to behave using the Resilient Testing Library.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceMockTest {

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

    private void stubCustomerAndProductExist() {
        FailureScenarios.stubReturns(() -> customerRepository.findById(1L), Optional.of(customer));
        FailureScenarios.stubReturns(() -> productRepository.findById(7L), Optional.of(product));
    }

    @Test
    void order_isConfirmed_whenStockAndPaymentSucceed() {
        stubCustomerAndProductExist();
        FailureScenarios.stubReturns(() -> inventoryClient.isInStock(7L, 2), true);
        FailureScenarios.stubReturns(() -> paymentClient.charge(anyString(), any()), PaymentResult.success());

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("1598.00"), order.getTotal());
        assertNull(order.getFailureReason());
        assertEquals(48, product.getStock());   // 2 items were taken from the stock of 50
        VerifyCalls.calledOnce(paymentClient).charge(anyString(), any());
    }

    @Test
    void order_fails_andPaymentIsNeverTried_whenOutOfStock() {
        stubCustomerAndProductExist();
        FailureScenarios.stubReturns(() -> inventoryClient.isInStock(7L, 2), false);

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(OrderService.OUT_OF_STOCK_MESSAGE, order.getFailureReason());
        assertEquals(50, product.getStock());   // stock is untouched
        VerifyCalls.neverCalled(paymentClient).charge(anyString(), any());
    }

    @Test
    void order_fails_withAFriendlyMessage_whenPaymentIsDeclined() {
        stubCustomerAndProductExist();
        FailureScenarios.stubReturns(() -> inventoryClient.isInStock(7L, 2), true);
        FailureScenarios.stubReturns(() -> paymentClient.charge(anyString(), any()),
                PaymentResult.declined("Card expired"));

        Order order = orderService.placeOrder(request);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertEquals(OrderService.PAYMENT_DECLINED_MESSAGE, order.getFailureReason());
        assertEquals(50, product.getStock());
        VerifyCalls.calledOnce(paymentClient).charge(anyString(), any());   // a decline is not retried
    }

    @Test
    void placingAnOrder_forAnUnknownCustomer_isRejected_andNothingIsSaved() {
        FailureScenarios.stubReturns(() -> customerRepository.findById(1L), Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.placeOrder(request));

        Mockito.verifyNoInteractions(orderRepository, paymentClient, inventoryClient);
    }
}
