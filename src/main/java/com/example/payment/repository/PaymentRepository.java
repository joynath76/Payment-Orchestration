package com.example.payment.repository;

import com.example.payment.model.PaymentEntity;
import java.util.Optional;

public interface PaymentRepository {
    PaymentEntity save(PaymentEntity payment);
    Optional<PaymentEntity> findById(String id);
}
