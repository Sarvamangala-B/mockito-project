package com.company.resilienttesting.failure;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

/**
 * Helpers that make ANY mocked call behave in a chosen way.
 * They work on any mock and never need to know what the call is for.
 *
 * How to use: pass the call as a "lambda", for example
 *     FailureScenarios.stubThrows(() -> someMock.someMethod(), new SimulatedTimeoutException());
 *
 * For methods that return NOTHING (void, such as "send"), use the stubVoid... versions:
 *     FailureScenarios.stubVoidThrows(someMock, m -> m.send(any()), new SimulatedTimeoutException());
 *
 * Tip: stub each mocked method once per test. Stubbing the same method again
 * calls it while it is already set to throw.
 */
public final class FailureScenarios {

    private FailureScenarios() {
        // helper class: not meant to be created
    }

    /** Make the mocked call answer with the given result. */
    public static <R> void stubReturns(Supplier<R> call, R result) {
        Mockito.when(call.get()).thenReturn(result);
    }

    /** Make the mocked call throw the given error every time. */
    public static <R> void stubThrows(Supplier<R> call, RuntimeException error) {
        Mockito.when(call.get()).thenThrow(error);
    }

    /** Make the mocked call answer only after the given delay (a slow response). */
    public static <R> void stubDelayed(Supplier<R> call, R result, Duration delay) {
        Mockito.when(call.get()).thenAnswer(invocation -> {
            Thread.sleep(delay.toMillis());
            return result;
        });
    }

    /** Make the mocked call fail the first "failures" times, and then answer with the result. */
    public static <R> void stubFailsThenReturns(Supplier<R> call, RuntimeException error,
                                                int failures, R result) {
        AtomicInteger calls = new AtomicInteger();
        Mockito.when(call.get()).thenAnswer(invocation -> {
            if (calls.incrementAndGet() <= failures) {
                throw error;
            }
            return result;
        });
    }

    // ------------------------------------------------------------------
    // Methods that return nothing (void). A void call has no result to
    // "stub", so these take the mock itself plus the call to break.
    // ------------------------------------------------------------------

    /** Make a void call throw the given error every time. */
    public static <T> void stubVoidThrows(T mock, Consumer<T> call, RuntimeException error) {
        call.accept(Mockito.doThrow(error).when(mock));
    }

    /** Make a void call fail the first "failures" times, and then work normally. */
    public static <T> void stubVoidFailsThenSucceeds(T mock, Consumer<T> call,
                                                     RuntimeException error, int failures) {
        Stubber stubber = failures > 0 ? Mockito.doThrow(error) : Mockito.doNothing();
        for (int i = 1; i < failures; i++) {
            stubber = stubber.doThrow(error);
        }
        if (failures > 0) {
            stubber = stubber.doNothing();
        }
        call.accept(stubber.when(mock));
    }

    /** Make a void call take the given time before it returns (a slow dependency). */
    public static <T> void stubVoidDelayed(T mock, Consumer<T> call, Duration delay) {
        call.accept(Mockito.doAnswer(invocation -> {
            Thread.sleep(delay.toMillis());
            return null;
        }).when(mock));
    }
}
