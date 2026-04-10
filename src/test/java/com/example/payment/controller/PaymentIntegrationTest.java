package com.example.payment.controller;

import com.example.payment.idempotency.IdempotencyException;
import com.example.payment.idempotency.IdempotencyService;
import com.example.payment.model.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IdempotencyService idempotencyService;

    @Test
    public void shouldProcessPaymentSuccessfully() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setIdempotencyKey("unique-key-1");
        request.setPreferredProvider("PROVIDER_B");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.provider").value("PROVIDER_B"));
    }

    @Test
    public void shouldReturnBadRequestWhenAmountIsMissing() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setCurrency("USD");
        request.setIdempotencyKey("unique-key-2");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    public void shouldReturnConflictWhenIdempotencyKeyIsDuplicate() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setIdempotencyKey("duplicate-key");

        doThrow(new IdempotencyException("Duplicate request")).when(idempotencyService).validateAndLock("duplicate-key");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Duplicate request"));
    }
}
