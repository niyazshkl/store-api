package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.PaymentException;

import java.util.Map;
import java.util.Optional;

public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookRequest(Map<String,String> headers, String payload) throws PaymentException;
}
