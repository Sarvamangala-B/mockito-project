package com.company.resilienttesting.failure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import com.company.resilienttesting.mock.MockUtils;
import com.company.resilienttesting.verification.VerifyCalls;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class FailureScenariosTest {

    /** A tiny made-up dependency, only used to test the helpers. */
    interface Greeter {
        String greet();
    }

    /** A made-up dependency whose method returns nothing (void). */
    interface Notifier {
        void notifyUser(String text);
    }

    private final Greeter greeter = MockUtils.createMock(Greeter.class);
    private final Notifier notifier = MockUtils.createMock(Notifier.class);

    @Test
    void stubReturns_makesTheMockAnswer() {
        FailureScenarios.stubReturns(greeter::greet, "hello");

        assertEquals("hello", greeter.greet());
    }

    @Test
    void stubThrows_makesTheMockFail() {
        FailureScenarios.stubThrows(greeter::greet, new SimulatedTimeoutException());

        assertThrows(SimulatedTimeoutException.class, greeter::greet);
    }

    @Test
    void stubFailsThenReturns_failsTwiceAndThenSucceeds() {
        FailureScenarios.stubFailsThenReturns(
                greeter::greet, new SimulatedServerErrorException(503), 2, "finally");

        assertThrows(SimulatedServerErrorException.class, greeter::greet);
        assertThrows(SimulatedServerErrorException.class, greeter::greet);
        assertEquals("finally", greeter.greet());
        assertEquals("finally", greeter.greet());
    }

    @Test
    void stubDelayed_answersOnlyAfterTheDelay() {
        FailureScenarios.stubDelayed(greeter::greet, "late", Duration.ofMillis(100));

        long start = System.nanoTime();
        String result = greeter.greet();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertEquals("late", result);
        assertTrue(elapsedMillis >= 90, "expected a delay of about 100 ms but it took " + elapsedMillis);
    }

    @Test
    void serverErrorException_remembersItsStatusCode() {
        assertEquals(500, new SimulatedServerErrorException(500).getStatusCode());
    }

    @Test
    void stubVoidThrows_makesAVoidCallFail() {
        FailureScenarios.stubVoidThrows(notifier, n -> n.notifyUser(anyString()), new SimulatedTimeoutException());

        assertThrows(SimulatedTimeoutException.class, () -> notifier.notifyUser("hi"));
    }

    @Test
    void stubVoidFailsThenSucceeds_failsTwiceAndThenWorks() {
        FailureScenarios.stubVoidFailsThenSucceeds(
                notifier, n -> n.notifyUser(anyString()), new SimulatedServerErrorException(503), 2);

        assertThrows(SimulatedServerErrorException.class, () -> notifier.notifyUser("one"));
        assertThrows(SimulatedServerErrorException.class, () -> notifier.notifyUser("two"));
        notifier.notifyUser("three");   // works now: no exception
        notifier.notifyUser("four");

        VerifyCalls.calledExactly(notifier, 4).notifyUser(anyString());
    }

    @Test
    void stubVoidDelayed_returnsOnlyAfterTheDelay() {
        FailureScenarios.stubVoidDelayed(notifier, n -> n.notifyUser(anyString()), Duration.ofMillis(100));

        long start = System.nanoTime();
        notifier.notifyUser("slow");
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis >= 90, "expected a delay of about 100 ms but it took " + elapsedMillis);
    }
}
