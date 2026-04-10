package com.example.payment.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String provider;
    private PaymentStatus status;
    private String errorMessage;
    private LocalDateTime timestamp;
}
