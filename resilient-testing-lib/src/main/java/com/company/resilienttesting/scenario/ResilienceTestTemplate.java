package com.company.resilienttesting.scenario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.company.resilienttesting.failure.SimulatedServerErrorException;
import com.company.resilienttesting.failure.SimulatedTimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A ready-made set of standard resilience checks. A team extends this class, fills in the four
 * short methods for its own dependency, and gets the three standard scenarios for free.
 *
 * The @Tag below is inherited, so these tests count as "resilience" tests automatically.
 *
 * @param <R> the type of result that the call under test returns
 */
@Tag("resilience")
public abstract class ResilienceTestTemplate<R> {

    /** Set up the dependency so that it answers normally. */
    protected abstract void stubDependencySucceeds();

    /** Set up the dependency so that it fails with the given error. */
    protected abstract void stubDependencyFails(RuntimeException error);

    /** Make the call that you are testing and return its result. */
    protected abstract R callUnderTest();

    /** Check that the failure was handled properly (no crash, a sensible outcome). */
    protected abstract void assertHandledGracefully(R result);

    @Test
    public void handlesTimeout() {
        stubDependencyFails(new SimulatedTimeoutException());
        assertHandledGracefully(callUnderTest());
    }

    @Test
    public void handlesServerError() {
        stubDependencyFails(new SimulatedServerErrorException(500));
        assertHandledGracefully(callUnderTest());
    }

    @Test
    public void worksWhenDependencyIsHealthy() {
        stubDependencySucceeds();
        assertDoesNotThrow(() -> {
            callUnderTest();
        });
    }
}
