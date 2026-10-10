package com.company.resilienttesting.verification;

import org.mockito.Mockito;

/**
 * Readable checks on how a mock was used. Write the call you expect right after it, for example
 *     VerifyCalls.calledExactly(someMock, 3).someMethod(any());
 */
public final class VerifyCalls {

    private VerifyCalls() {
        // helper class: not meant to be created
    }

    public static <T> T calledExactly(T mock, int times) {
        return Mockito.verify(mock, Mockito.times(times));
    }

    public static <T> T calledOnce(T mock) {
        return calledExactly(mock, 1);
    }

    public static <T> T calledAtLeast(T mock, int times) {
        return Mockito.verify(mock, Mockito.atLeast(times));
    }

    public static <T> T neverCalled(T mock) {
        return Mockito.verify(mock, Mockito.never());
    }
}
