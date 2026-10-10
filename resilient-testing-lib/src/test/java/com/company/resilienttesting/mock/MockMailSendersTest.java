package com.company.resilienttesting.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.company.resilienttesting.failure.SimulatedServerErrorException;
import com.company.resilienttesting.failure.SimulatedTimeoutException;
import com.company.resilienttesting.mock.MockMailSenders.RecordingMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

class MockMailSendersTest {

    private final RecordingMailSender mailbox = MockMailSenders.recording();

    private static SimpleMailMessage message(String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("someone@example.test");
        message.setSubject(subject);
        return message;
    }

    @Test
    void startsWithNothingSent() {
        assertEquals(0, mailbox.sentCount());
    }

    @Test
    void remembersASingleMessage() {
        mailbox.mailSender().send(message("Hello"));

        assertEquals(1, mailbox.sentCount());
        assertEquals("Hello", mailbox.sentMessages().get(0).getSubject());
    }

    @Test
    void remembersSeveralMessagesSentTogether() {
        mailbox.mailSender().send(message("First"), message("Second"));

        assertEquals(2, mailbox.sentCount());
        assertEquals("Second", mailbox.sentMessages().get(1).getSubject());
    }

    @Test
    void canBeToldToAlwaysFail_andThenSendsNothing() {
        mailbox.alwaysFailWith(new SimulatedTimeoutException());

        assertThrows(SimulatedTimeoutException.class, () -> mailbox.mailSender().send(message("Lost")));
        assertEquals(0, mailbox.sentCount());
    }

    @Test
    void canFailTwiceAndThenWorkAgain() {
        mailbox.failTheNext(2, new SimulatedServerErrorException(503));

        assertThrows(SimulatedServerErrorException.class, () -> mailbox.mailSender().send(message("a")));
        assertThrows(SimulatedServerErrorException.class, () -> mailbox.mailSender().send(message("b")));
        mailbox.mailSender().send(message("c"));

        assertEquals(1, mailbox.sentCount());
        assertEquals("c", mailbox.sentMessages().get(0).getSubject());
    }

    @Test
    void stopFailing_makesItWorkAgain() {
        mailbox.alwaysFailWith(new SimulatedTimeoutException());
        mailbox.stopFailing();

        mailbox.mailSender().send(message("Back"));

        assertEquals(1, mailbox.sentCount());
    }
}
