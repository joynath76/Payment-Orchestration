package com.example.payment.service;

import com.example.payment.idempotency.IdempotencyException;
import com.example.payment.idempotency.IdempotencyService;
import com.example.payment.model.PaymentEntity;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentResponse;
import com.example.payment.model.PaymentStatus;
import com.example.payment.provider.PaymentProvider;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.routing.RoutingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private OrchestrationService orchestrationService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private RoutingEngine routingEngine;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(paymentProvider.getName()).thenReturn("PROVIDER_A");
        when(paymentProvider.isAvailable()).thenReturn(true);
        paymentService = new PaymentService(paymentRepository, orchestrationService, idempotencyService, routingEngine);
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setIdempotencyKey("unique-key-1");
        request.setPaymentMethod("CARD");
        request.setPreferredProvider("PROVIDER_A");

        PaymentEntity initialPayment = PaymentEntity.builder()
                .id("test-id")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(initialPayment);
        when(routingEngine.selectProvider("CARD", "PROVIDER_A")).thenReturn(paymentProvider);
        when(orchestrationService.processWithRetry(any(), any())).thenReturn(true);

        // When
        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("PROVIDER_A", response.getProvider());
        verify(paymentRepository, atLeastOnce()).save(any(PaymentEntity.class));
        verify(orchestrationService).processWithRetry(eq(paymentProvider), any(PaymentEntity.class));
        verify(idempotencyService).validateAndLock("unique-key-1");
        verify(idempotencyService).updateStatus("unique-key-1", "SUCCESS");
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyViolation() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setIdempotencyKey("duplicate-key");

        doThrow(new IdempotencyException("Violation")).when(idempotencyService).validateAndLock("duplicate-key");

        // When & Then
        assertThrows(IdempotencyException.class, () -> paymentService.processPayment(request));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldHandlePaymentFailureFromProvider() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setIdempotencyKey("key-fail");
        request.setPaymentMethod("CARD");
        request.setPreferredProvider("PROVIDER_A");

        PaymentEntity initialPayment = PaymentEntity.builder()
                .id("test-id")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(initialPayment);
        when(routingEngine.selectProvider("CARD", "PROVIDER_A")).thenReturn(paymentProvider);
        when(orchestrationService.processWithRetry(any(), any())).thenReturn(false);

        // When
        PaymentResponse response = paymentService.processPayment(request);

        // Then
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        verify(idempotencyService).updateStatus("key-fail", "FAILED");
    }
}
