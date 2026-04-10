package com.example.payment.service;

import com.example.payment.model.PaymentEntity;
import com.example.payment.provider.PaymentProvider;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrchestrationService {

    @Retry(name = "paymentRetry", fallbackMethod = "fallbackPayment")
    public boolean processWithRetry(PaymentProvider provider, PaymentEntity payment) {
        log.debug("Attempting payment for id: {} using provider: {}", payment.getId(), provider.getName());
        return provider.process(payment);
    }

    public boolean fallbackPayment(PaymentProvider provider, PaymentEntity payment, Throwable t) {
        log.error("All retry attempts failed for paymentId: {}. Reason: {}", payment.getId(), t.getMessage());
        throw new RuntimeException("Payment processing failed after all retry attempts: " + t.getMessage(), t);
    }
}
