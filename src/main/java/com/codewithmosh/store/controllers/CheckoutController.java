package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.CheckoutResponseDto;
import com.codewithmosh.store.dtos.checkout.RequestCheckoutDto;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/checkout")
@AllArgsConstructor
public class CheckoutController {
    private CheckoutService checkoutService;
    @PostMapping
    public ResponseEntity<CheckoutResponseDto> checkout(
            @Valid @RequestBody RequestCheckoutDto requestCheckoutDto
            ) {
            UUID cartId = requestCheckoutDto.getCartId();
            return  ResponseEntity.ok(checkoutService.checkout(cartId));
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
