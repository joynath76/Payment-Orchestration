package com.example.payment.provider;

import com.example.payment.model.PaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component("PROVIDER_A")
public class ProviderA implements PaymentProvider {

    private final Random random = new Random();

    @Override
    public boolean process(PaymentEntity payment) {
        log.info("ProviderA: Processing payment for amount {} {}", payment.getAmount(), payment.getCurrency());
        
        // Simulating processing logic
        // 70% success rate
        if (random.nextDouble() < 0.7) {
            log.info("ProviderA: Payment successful");
            return true;
        } else {
            log.warn("ProviderA: Payment failed or timeout");
            throw new RuntimeException("ProviderA Service Unavailable");
        }
    }

    @Override
    public String getName() {
        return "PROVIDER_A";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
