package com.example.payment.service;

public class PaymentProcessingException extends RuntimeException {
    private final String paymentId;

    public PaymentProcessingException(String message, String paymentId) {
        super(message);
        this.paymentId = paymentId;
    }

    public PaymentProcessingException(String message, String paymentId, Throwable cause) {
        super(message, cause);
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
