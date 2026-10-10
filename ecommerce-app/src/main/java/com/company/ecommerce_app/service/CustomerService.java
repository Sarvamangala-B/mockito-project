package com.company.ecommerce_app.service;

import com.company.ecommerce_app.dto.CustomerResponse;
import com.company.ecommerce_app.dto.CustomerResponse.OrderSummary;
import com.company.ecommerce_app.exception.NotFoundException;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public CustomerResponse getCustomerWithOrders(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer " + id + " was not found."));

        List<OrderSummary> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(id).stream()
                .map(order -> new OrderSummary(
                        order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt()))
                .toList();

        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(), orders);
    }
}
