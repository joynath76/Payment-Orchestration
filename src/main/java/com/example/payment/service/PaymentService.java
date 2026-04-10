package com.example.payment.service;

import com.example.payment.idempotency.IdempotencyService;
import com.example.payment.model.PaymentEntity;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentResponse;
import com.example.payment.model.PaymentStatus;
import com.example.payment.provider.PaymentProvider;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.routing.RoutingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrchestrationService orchestrationService;
    private final IdempotencyService idempotencyService;
    private final RoutingEngine routingEngine;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment request with idempotency key: {}", request.getIdempotencyKey());

        // 1. Idempotency Check
        idempotencyService.validateAndLock(request.getIdempotencyKey());

        try {
            // 2. Initial entity creation
            PaymentEntity payment = PaymentEntity.builder()
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .paymentMethod(request.getPaymentMethod())
                    .idempotencyKey(request.getIdempotencyKey())
                    .status(PaymentStatus.PENDING)
                    .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);

            // 3. Select provider (Routing)
            PaymentProvider provider = routingEngine.selectProvider(request.getPaymentMethod(), request.getPreferredProvider());
            savedPayment.setProvider(provider.getName());
            paymentRepository.save(savedPayment);

            try {
                // 4. Process payment with retry via OrchestrationService
                boolean success = orchestrationService.processWithRetry(provider, savedPayment);
                
                if (success) {
                    savedPayment.setStatus(PaymentStatus.SUCCESS);
                } else {
                    savedPayment.setStatus(PaymentStatus.FAILED);
                    savedPayment.setErrorMessage("Provider returned failure status");
                }
            } catch (Exception e) {
                log.error("Payment processing failed after retries for paymentId: {}", savedPayment.getId(), e);
                savedPayment.setStatus(PaymentStatus.FAILED);
                savedPayment.setErrorMessage(e.getMessage());
            }

            PaymentEntity finalPayment = paymentRepository.save(savedPayment);
            
            // 5. Update Idempotency status
            idempotencyService.updateStatus(request.getIdempotencyKey(), finalPayment.getStatus().name());
            
            return mapToResponse(finalPayment);
            
        } catch (Exception e) {
            log.error("Error during payment orchestration for idempotency key: {}", request.getIdempotencyKey(), e);
            throw e;
        }
    }

    private PaymentResponse mapToResponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .status(payment.getStatus())
                .errorMessage(payment.getErrorMessage())
                .timestamp(payment.getUpdatedAt())
                .build();
    }

    public Optional<PaymentResponse> getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::mapToResponse);
    }
}
