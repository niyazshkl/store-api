package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.CheckoutResponseDto;
import com.codewithmosh.store.dtos.checkout.RequestCheckoutDto;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.services.CheckoutService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import jdk.jfr.Event;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @PostMapping
    public ResponseEntity<CheckoutResponseDto> checkout(
            @Valid @RequestBody RequestCheckoutDto requestCheckoutDto
            ) {
            UUID cartId = requestCheckoutDto.getCartId();
            return  ResponseEntity.ok(checkoutService.checkout(cartId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("Stripe-signature") String signature,
            @RequestBody String payload){

        try {
            var event = Webhook.constructEvent(payload,signature,webhookSecretKey);
            System.out.println(event.getType());

            var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

            switch (event.getType()){
                case "payment_intent.succeeded" -> {
                    //update order status to PAID
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    if (paymentIntent!=null){
                        String orderId = paymentIntent.getMetadata().get("orderId");
                        Order order =  orderRepository.findById(Long.valueOf(orderId)).orElse(null);
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                    }
                }
                case "payment_intent.failed" -> {
                    //update order status to FAILED
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    if (paymentIntent!=null){
                        String orderId = paymentIntent.getMetadata().get("orderId");
                        Order order =  orderRepository.findById(Long.valueOf(orderId)).orElse(null);
                        order.setStatus(OrderStatus.FAILED);
                        orderRepository.save(order);
                    }
                }
            }
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String,String>> handlePaymentException(PaymentException exception){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleCartNotFound(CartNotFoundException exception){
        return ResponseEntity.badRequest().body(Map.of("Error", "Cart not found"));
    }
    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<Map<String,String>> handleCartEmpty(CartEmptyException exception){
        return ResponseEntity.badRequest().body(Map.of("Error", "Cart empty"));
    }
}
