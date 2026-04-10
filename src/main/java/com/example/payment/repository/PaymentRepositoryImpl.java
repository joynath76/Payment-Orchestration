package com.example.payment.repository;

import com.example.payment.model.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public PaymentEntity save( PaymentEntity payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentEntity> findById(String id) {
        return jpaRepository.findById(id);
    }
}
