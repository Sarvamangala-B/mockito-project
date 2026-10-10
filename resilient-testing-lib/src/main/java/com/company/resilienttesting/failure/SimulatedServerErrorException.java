package com.company.resilienttesting.failure;

/** A stand-in for "the other side answered with a server error (for example HTTP 500)". */
public class SimulatedServerErrorException extends RuntimeException {

    private final int statusCode;

    public SimulatedServerErrorException(int statusCode) {
        super("Simulated server error (HTTP " + statusCode + ").");
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
