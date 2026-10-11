package com.company.ecommerce_app.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.company.ecommerce_app.dto.CustomerResponse;
import com.company.ecommerce_app.exception.NotFoundException;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderStatus;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.OrderRepository;
import com.company.ecommerce_app.service.CustomerService;
import com.company.ecommerce_app.support.TestData;
import com.company.resilienttesting.failure.FailureScenarios;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    CustomerService customerService;

    private Order orderWith(Customer customer, long id, OrderStatus status, String total) {
        Order order = new Order(customer, new BigDecimal(total));
        ReflectionTestUtils.setField(order, "id", id);
        order.setStatus(status);
        return order;
    }

    @Test
    void theProfile_includesTheOrderHistory_inTheOrderTheRepositoryGivesIt() {
        Customer customer = TestData.customer(1L);
        Order newest = orderWith(customer, 12L, OrderStatus.FAILED, "50.00");
        Order oldest = orderWith(customer, 11L, OrderStatus.CONFIRMED, "100.00");
        FailureScenarios.stubReturns(() -> customerRepository.findById(1L), Optional.of(customer));
        FailureScenarios.stubReturns(
                () -> orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L), List.of(newest, oldest));

        CustomerResponse profile = customerService.getCustomerWithOrders(1L);

        assertEquals(Long.valueOf(1L), profile.id());
        assertEquals("Asha Verma", profile.name());
        assertEquals("asha@example.com", profile.email());
        assertEquals(2, profile.orders().size());
        assertEquals(Long.valueOf(12L), profile.orders().get(0).orderId());
        assertEquals(OrderStatus.FAILED, profile.orders().get(0).status());
        assertEquals(new BigDecimal("100.00"), profile.orders().get(1).total());
    }

    @Test
    void anUnknownCustomer_reportsNotFound_andLooksUpNoOrders() {
        FailureScenarios.stubReturns(() -> customerRepository.findById(99L), Optional.empty());

        assertThrows(NotFoundException.class, () -> customerService.getCustomerWithOrders(99L));

        Mockito.verifyNoInteractions(orderRepository);
    }
}
