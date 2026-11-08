package com.codewithmosh.store.payments;

import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.repositories.OrderRepository;
import jakarta.validation.Valid;
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
            @RequestHeader Map<String,String> headers,
            @RequestBody String payload){
        checkoutService.handleWebhookEvent(headers,payload);
        return ResponseEntity.ok().build();
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
