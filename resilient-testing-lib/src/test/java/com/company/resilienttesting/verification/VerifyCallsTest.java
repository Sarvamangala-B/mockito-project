package com.company.resilienttesting.verification;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.company.resilienttesting.mock.MockUtils;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.base.MockitoAssertionError;

class VerifyCallsTest {

    /** A tiny made-up dependency, only used to test the helpers. */
    interface Greeter {
        String greet();
    }

    private final Greeter greeter = MockUtils.createMock(Greeter.class);

    @Test
    void calledExactly_passesWhenTheCountMatches() {
        greeter.greet();
        greeter.greet();

        VerifyCalls.calledExactly(greeter, 2).greet();
    }

    @Test
    void calledExactly_failsWhenTheCountDiffers() {
        greeter.greet();

        assertThrows(MockitoAssertionError.class, () -> VerifyCalls.calledExactly(greeter, 3).greet());
    }

    @Test
    void calledOnce_passesForASingleCall() {
        greeter.greet();

        VerifyCalls.calledOnce(greeter).greet();
    }

    @Test
    void calledAtLeast_passesWhenCalledMoreOften() {
        greeter.greet();
        greeter.greet();
        greeter.greet();

        VerifyCalls.calledAtLeast(greeter, 2).greet();
    }

    @Test
    void neverCalled_passesWhenNothingWasCalled() {
        VerifyCalls.neverCalled(greeter).greet();
    }

    @Test
    void neverCalled_failsWhenSomethingWasCalled() {
        greeter.greet();

        assertThrows(MockitoAssertionError.class, () -> VerifyCalls.neverCalled(greeter).greet());
    }
}
