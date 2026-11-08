package com.codewithmosh.store.payments;

import com.codewithmosh.store.entities.Order;

import java.util.Map;
import java.util.Optional;

public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookRequest(Map<String,String> headers, String payload) throws PaymentException;
}
