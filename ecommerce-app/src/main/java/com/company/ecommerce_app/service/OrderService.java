package com.company.ecommerce_app.service;

import com.company.ecommerce_app.client.InventoryClient;
import com.company.ecommerce_app.client.PaymentClient;
import com.company.ecommerce_app.client.PaymentResult;
import com.company.ecommerce_app.client.PaymentTimeoutException;
import com.company.ecommerce_app.dto.OrderRequest;
import com.company.ecommerce_app.exception.NotFoundException;
import com.company.ecommerce_app.model.Customer;
import com.company.ecommerce_app.model.Order;
import com.company.ecommerce_app.model.OrderItem;
import com.company.ecommerce_app.model.OrderStatus;
import com.company.ecommerce_app.model.Product;
import com.company.ecommerce_app.repository.CustomerRepository;
import com.company.ecommerce_app.repository.OrderRepository;
import com.company.ecommerce_app.repository.ProductRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    static final String OUT_OF_STOCK_MESSAGE = "Sorry, this product is out of stock.";
    static final String PAYMENT_DECLINED_MESSAGE = "Payment was declined.";
    static final String PAYMENT_UNAVAILABLE_MESSAGE = "Payment service is unavailable. Please try again.";

    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    // RESILIENCE RULE: the payment is tried at most this many times in total
    // (the first try plus the retries). Change it in application.properties.
    @Value("${payment.max-attempts:3}")
    private int maxPaymentAttempts = 3;

    public OrderService(PaymentClient paymentClient,
                        InventoryClient inventoryClient,
                        OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.paymentClient = paymentClient;
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer " + request.customerId() + " was not found."));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product " + request.productId() + " was not found."));

        BigDecimal total = calculateTotal(product.getPrice(), request.quantity());

        // Save the order first, as PENDING, so that it always has an id.
        Order order = new Order(customer, total);
        order.addItem(new OrderItem(product, request.quantity(), product.getPrice()));
        orderRepository.save(order);

        // 1. Check stock
        if (!inventoryClient.isInStock(product.getId(), request.quantity())) {
            return fail(order, OUT_OF_STOCK_MESSAGE);
        }

        // 2. Charge payment (with retries when the payment service times out)
        PaymentResult result;
        try {
            result = chargeWithRetry(order, total);
        } catch (PaymentTimeoutException e) {
            return fail(order, PAYMENT_UNAVAILABLE_MESSAGE);
        }
        if (!result.successful()) {
            return fail(order, PAYMENT_DECLINED_MESSAGE);
        }

        // 3. Everything worked: reserve the stock and confirm the order
        product.setStock(product.getStock() - request.quantity());
        productRepository.save(product);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        return order;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " was not found."));
    }

    static BigDecimal calculateTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private PaymentResult chargeWithRetry(Order order, BigDecimal amount) {
        String orderRef = "ORDER-" + order.getId();
        for (int attempt = 1; attempt <= maxPaymentAttempts; attempt++) {
            try {
                return paymentClient.charge(orderRef, amount);
            } catch (PaymentTimeoutException e) {
                log.warn("Payment attempt {} of {} timed out for {}", attempt, maxPaymentAttempts, orderRef);
                if (attempt == maxPaymentAttempts) {
                    throw e;   // out of attempts: let placeOrder mark the order FAILED
                }
            }
        }
        throw new IllegalStateException("payment.max-attempts must be at least 1");
    }

    private Order fail(Order order, String message) {
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(message);
        orderRepository.save(order);
        return order;
    }
}
