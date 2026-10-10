package com.company.resilienttesting.mock;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.Mockito;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * A ready-made fake mail sender (Spring's MailSender). It sends nothing, remembers every message
 * it was asked to send, and can be told to fail so that you can test how your code copes.
 */
public final class MockMailSenders {

    private MockMailSenders() {
        // helper class: not meant to be created
    }

    /** Creates a fake mail sender that works normally and records what it "sent". */
    public static RecordingMailSender recording() {
        return new RecordingMailSender();
    }

    /** The fake mail sender plus the list of messages it has "sent". */
    public static final class RecordingMailSender {

        private final List<SimpleMailMessage> sent = new CopyOnWriteArrayList<>();
        private final AtomicInteger failuresLeft = new AtomicInteger(0);   // -1 means "always fail"
        private volatile RuntimeException failure;
        private final MailSender mailSender;

        private RecordingMailSender() {
            this.mailSender = Mockito.mock(MailSender.class, invocation -> {
                if (!invocation.getMethod().getName().equals("send")) {
                    return Mockito.RETURNS_DEFAULTS.answer(invocation);
                }
                if (failuresLeft.get() != 0) {
                    if (failuresLeft.get() > 0) {
                        failuresLeft.decrementAndGet();
                    }
                    throw failure;
                }
                for (Object argument : invocation.getArguments()) {
                    if (argument instanceof SimpleMailMessage message) {
                        sent.add(message);
                    } else if (argument instanceof SimpleMailMessage[] messages) {
                        sent.addAll(Arrays.asList(messages));
                    }
                }
                return null;
            });
        }

        /** The fake itself: give this to the code you are testing. */
        public MailSender mailSender() {
            return mailSender;
        }

        /** Every message that was sent successfully, oldest first. */
        public List<SimpleMailMessage> sentMessages() {
            return List.copyOf(sent);
        }

        public int sentCount() {
            return sent.size();
        }

        /** From now on every send fails with this error. */
        public void alwaysFailWith(RuntimeException error) {
            this.failure = error;
            this.failuresLeft.set(-1);
        }

        /** The next "times" sends fail with this error, and later ones work again. */
        public void failTheNext(int times, RuntimeException error) {
            this.failure = error;
            this.failuresLeft.set(times);
        }

        /** Go back to working normally. */
        public void stopFailing() {
            this.failuresLeft.set(0);
        }
    }
}
