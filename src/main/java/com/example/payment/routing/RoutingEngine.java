package com.example.payment.routing;

import com.example.payment.provider.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingEngine {

    private final Map<String, PaymentProvider> paymentProviders;

    /**
     * Smart provider routing strategy based on payment method.
     * CARD → Provider A, UPI → Provider B
     * If preferred provider is requested and available, use it.
     * Otherwise, use payment method based routing.
     */
    public PaymentProvider selectProvider(String paymentMethod, String preferredProvider) {
        log.info("Routing request. Payment method: {}, Preferred provider: {}", paymentMethod, preferredProvider);

        // Check if preferred provider is requested
        if (preferredProvider != null && !preferredProvider.isBlank()) {
            PaymentProvider preferred = paymentProviders.get(preferredProvider.toUpperCase());
            if (preferred != null && preferred.isAvailable()) {
                log.info("Using preferred provider: {}", preferred.getName());
                return preferred;
            }
        }

        // Route based on payment method
        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            PaymentProvider providerA = paymentProviders.get("PROVIDER_A");
            if (providerA != null && providerA.isAvailable()) {
                log.info("Routing CARD payment to Provider A");
                return providerA;
            }
        } else if ("UPI".equalsIgnoreCase(paymentMethod)) {
            PaymentProvider providerB = paymentProviders.get("PROVIDER_B");
            if (providerB != null && providerB.isAvailable()) {
                log.info("Routing UPI payment to Provider B");
                return providerB;
            }
        }

        // Fallback to any available provider
        return paymentProviders.values().stream()
                .filter(PaymentProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available payment providers"));
    }
}
