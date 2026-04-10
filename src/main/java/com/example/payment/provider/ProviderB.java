package com.example.payment.provider;

import com.example.payment.model.PaymentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component("PROVIDER_B")
public class ProviderB implements PaymentProvider {

    private final Random random = new Random();

    @Override
    public boolean process(PaymentEntity payment) {
        log.info("ProviderB: Processing payment for amount {} {}", payment.getAmount(), payment.getCurrency());
        
        // Simulating processing logic
        // 80% success rate
        if (random.nextDouble() < 0.8) {
            log.info("ProviderB: Payment successful");
            return true;
        } else {
            log.warn("ProviderB: Payment failed or timeout");
            throw new RuntimeException("ProviderB API Timeout");
        }
    }

    @Override
    public String getName() {
        return "PROVIDER_B";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
