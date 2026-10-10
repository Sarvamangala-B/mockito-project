package com.company.resilienttesting.mock;

import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/** A ready-made fake RestTemplate (Spring's tool for calling other web services). */
public final class MockRestClients {

    private MockRestClients() {
        // helper class: not meant to be created
    }

    /**
     * A fake RestTemplate that answers every exchange(...), getForEntity(...), postForEntity(...)
     * and similar call with the given status and body, and every getForObject(...) and
     * postForObject(...) call with the body.
     */
    public static RestTemplate returning(HttpStatus status, Object body) {
        return Mockito.mock(RestTemplate.class, invocation -> {
            String method = invocation.getMethod().getName();
            if (method.equals("exchange") || method.endsWith("ForEntity")) {
                return ResponseEntity.status(status).body(body);
            }
            if (method.endsWith("ForObject")) {
                return body;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        });
    }
}
