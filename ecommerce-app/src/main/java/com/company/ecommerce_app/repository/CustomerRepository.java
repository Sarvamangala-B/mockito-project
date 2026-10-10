package com.company.ecommerce_app.repository;

import com.company.ecommerce_app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
