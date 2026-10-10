package com.company.ecommerce_app.repository;

import com.company.ecommerce_app.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Spring writes the SQL for this from the method name:
    // all orders of one customer, newest first
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
