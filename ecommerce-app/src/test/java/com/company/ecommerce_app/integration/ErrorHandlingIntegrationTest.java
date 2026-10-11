package com.company.ecommerce_app.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Mistakes by the caller always give a clean, readable error. */
@Tag("integration")
@SpringBootTest(properties = "simulation.payment.mode=SUCCESS")
@AutoConfigureMockMvc
class ErrorHandlingIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void brokenJson_gives400() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("JSON")));
    }

    @Test
    void anUnknownCustomer_gives404() throws Exception {
        mockMvc.perform(get("/customers/{id}", 999999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void anUnknownOrder_gives404() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
