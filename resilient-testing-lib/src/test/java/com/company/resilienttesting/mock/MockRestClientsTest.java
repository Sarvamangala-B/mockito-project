package com.company.resilienttesting.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class MockRestClientsTest {

    @Test
    void getForEntity_returnsTheChosenStatusAndBody() {
        RestTemplate rest = MockRestClients.returning(HttpStatus.OK, "hello");

        ResponseEntity<String> response = rest.getForEntity("http://example.test/anything", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("hello", response.getBody());
    }

    @Test
    void getForEntity_canReturnAnErrorStatus() {
        RestTemplate rest = MockRestClients.returning(HttpStatus.INTERNAL_SERVER_ERROR, "boom");

        ResponseEntity<String> response = rest.getForEntity("http://example.test/anything", String.class);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("boom", response.getBody());
    }

    @Test
    void getForObject_returnsTheBody() {
        RestTemplate rest = MockRestClients.returning(HttpStatus.OK, "plain body");

        assertEquals("plain body", rest.getForObject("http://example.test/anything", String.class));
    }
}
