package com.example.payment.provider;

import com.example.payment.model.PaymentEntity;

public interface PaymentProvider {
    /**
     * Process a payment request asynchronously or synchronously.
     * @param payment The payment details
     * @return true if successful, false or throws exception if failed
     */
    boolean process(PaymentEntity payment);
    
    /**
     * @return the unique name of this provider (e.g., "STRIPE", "PAYPAL")
     */
    String getName();
    
    /**
     * @return whether this provider is available/active
     */
    boolean isAvailable();
}
