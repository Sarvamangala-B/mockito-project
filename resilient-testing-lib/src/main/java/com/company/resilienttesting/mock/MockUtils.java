package com.company.resilienttesting.mock;

import org.mockito.Mockito;

/** Creates a generic mock of any class or interface. Use it when no ready-made mock exists. */
public final class MockUtils {

    private MockUtils() {
        // helper class: not meant to be created
    }

    public static <T> T createMock(Class<T> type) {
        return Mockito.mock(type);
    }
}
