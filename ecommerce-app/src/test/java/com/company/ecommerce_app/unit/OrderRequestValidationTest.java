package com.company.ecommerce_app.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.ecommerce_app.dto.OrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Checks the rules for an incoming order request. */
class OrderRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aCompleteRequest_isValid() {
        assertTrue(validator.validate(new OrderRequest(1L, 7L, 2)).isEmpty());
    }

    @Test
    void aQuantityOfZero_isRejected() {
        Set<ConstraintViolation<OrderRequest>> problems = validator.validate(new OrderRequest(1L, 7L, 0));

        assertEquals(1, problems.size());
        assertEquals("quantity", problems.iterator().next().getPropertyPath().toString());
    }

    @Test
    void aMissingCustomer_isRejected() {
        Set<ConstraintViolation<OrderRequest>> problems = validator.validate(new OrderRequest(null, 7L, 2));

        assertEquals(1, problems.size());
        assertEquals("customerId", problems.iterator().next().getPropertyPath().toString());
    }
}
