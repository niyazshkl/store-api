package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.carts.AddItemToCartRequest;
import com.codewithmosh.store.dtos.carts.CartDto;
import com.codewithmosh.store.dtos.carts.CartItemDto;
import com.codewithmosh.store.dtos.carts.UpdateCartItemRequestDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
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
public class CartController {
    private final CartMapper cartMapper;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<CartDto> createCart(UriComponentsBuilder uriComponentsBuilder){
        Cart cart = new Cart();
        cartRepository.save(cart);
        URI uri = uriComponentsBuilder.path("/carts/{id}").buildAndExpand(cart.getId()).toUri();
        return ResponseEntity.created(uri).body(cartMapper.toDto(cart));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable(name = "cartId") UUID cartId){
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart==null) return ResponseEntity.notFound().build();
        CartDto cartDto = cartMapper.toDto(cart);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addProductToCart(@PathVariable(name = "cartId") UUID cartId,
                                                  @Valid @RequestBody AddItemToCartRequest addItemToCartRequest,
                                                 UriComponentsBuilder uriComponentsBuilder){
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart==null) return ResponseEntity.notFound().build();

        Product product = productRepository.findById(addItemToCartRequest.getProductId()).orElse(null);
        if (product==null) return ResponseEntity.badRequest().build();

        CartItem cartItem = cart.addProduct(product);
        cartRepository.save(cart);

        CartItemDto cartItemDto = cartMapper.toDto(cartItem); //nullPointerException

        URI uri = uriComponentsBuilder
                .path("/carts/{cartId}/items/{cartItemId}")
                .buildAndExpand(cart.getId(), cartItem.getId())
                .toUri();
        return ResponseEntity.created(uri).body(cartItemDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateCartItem
            (@PathVariable(name = "cartId") UUID cartId,
             @PathVariable(name = "productId") Long productId,
             @Valid @RequestBody UpdateCartItemRequestDto updateCartItemRequestDto){
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("Error","Cart not found")
        );

        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("Error","Product not found")
        );

        Integer updated_quantity = updateCartItemRequestDto.getQuantity();

        CartItem cartItem = cart.updateCartItem(product, updated_quantity);
        if (cartItem ==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("Error","CartItem containing the product not found")
        );

        cartRepository.save(cart);

        CartItemDto cartItemDto = cartMapper.toDto(cartItem);

        return ResponseEntity.ok(cartItemDto);
    }

}
