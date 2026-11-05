package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.carts.AddItemToCartRequest;
import com.codewithmosh.store.dtos.carts.CartDto;
import com.codewithmosh.store.dtos.carts.CartItemDto;
import com.codewithmosh.store.dtos.carts.UpdateCartItemRequestDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.exceptions.CartItemNotFoundException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/carts")
@AllArgsConstructor
@Tag(name = "Carts") //swaggerUI decorator
public class CartController {
    private final CartService cartService;

    @PostMapping
    @Operation(summary = "Create a new cart") //swaggerUI decorator
    public ResponseEntity<CartDto> createCart(UriComponentsBuilder uriComponentsBuilder){
        CartDto cartDto = cartService.createCart();
        URI uri = uriComponentsBuilder.path("/carts/{id}").buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Find an existing cart")
    public ResponseEntity<CartDto> getCart(
            @Parameter(description = "The UUID of the cart") //swaggerUI decorator
            @PathVariable(name = "cartId") UUID cartId){
        CartDto cartDto = cartService.getCart(cartId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addProductToCart(@PathVariable(name = "cartId") UUID cartId,
                                                  @Valid @RequestBody AddItemToCartRequest addItemToCartRequest){
        CartItemDto cartItemDto = cartService.addProduct(cartId, addItemToCartRequest.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateCartItem
            (@PathVariable(name = "cartId") UUID cartId,
             @PathVariable(name = "productId") Long productId,
             @Valid @RequestBody UpdateCartItemRequestDto updateCartItemRequestDto){
        CartItemDto cartItemDto = cartService.updateCartItem(cartId, productId, updateCartItemRequestDto.getQuantity());
        return ResponseEntity.ok(cartItemDto);
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<?> clearCart
            (@PathVariable(name = "cartId") UUID cartId){
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> removeProductFromCart
            (@PathVariable(name = "cartId") UUID cartId,
             @PathVariable(name = "productId") Long productId) {
        cartService.removeProduct(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(CartNotFoundException cartNotFoundException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("Error", "Cart not found")
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(ProductNotFoundException productNotFoundException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("Error", "Product was not found")
        );
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartItemNotFound(CartItemNotFoundException productNotFoundException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("Error", "Cart item was not found")
        );
    }
}
