package com.company.resilienttesting.failure;

/** A stand-in for "the other side did not answer in time". Used when simulating a timeout. */
public class SimulatedTimeoutException extends RuntimeException {

    public SimulatedTimeoutException() {
        super("Simulated timeout: the dependency did not answer in time.");
    }

    public SimulatedTimeoutException(String message) {
        super(message);
    }
}
