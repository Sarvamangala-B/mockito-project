package com.company.resilienttesting.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.resilienttesting.failure.FailureScenarios;
import com.company.resilienttesting.mock.MockUtils;

/**
 * Proves that the template works by using it on a made-up dependency.
 * The three standard scenarios are inherited and run automatically.
 */
class ResilienceTestTemplateTest extends ResilienceTestTemplate<String> {

    interface Greeter {
        String greet();
    }

    private final Greeter greeter = MockUtils.createMock(Greeter.class);

    /** The "code under test": it should fall back gracefully when the greeter fails. */
    private String greetOrFallBack() {
        try {
            return greeter.greet();
        } catch (RuntimeException e) {
            return "fallback";
        }
    }

    @Override
    protected void stubDependencySucceeds() {
        FailureScenarios.stubReturns(greeter::greet, "hello");
    }

    @Override
    protected void stubDependencyFails(RuntimeException error) {
        FailureScenarios.stubThrows(greeter::greet, error);
    }

    @Override
    protected String callUnderTest() {
        return greetOrFallBack();
    }

    @Override
    protected void assertHandledGracefully(String result) {
        assertEquals("fallback", result);
    }
}
