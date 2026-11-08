package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.carts.CartDto;
import com.codewithmosh.store.dtos.carts.CartItemDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.products.Product;
import com.codewithmosh.store.exceptions.cart.CartItemNotFoundException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.exceptions.cart.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.products.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    public CartDto createCart() {
        Cart cart = new Cart();
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    public CartDto getCart(UUID cartId){
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart==null) throw new CartNotFoundException();
        CartDto cartDto = cartMapper.toDto(cart);
        return cartDto;
    }

    public CartItemDto addProduct(UUID cartId, Long productId){
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart==null) throw new CartNotFoundException();

        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) throw new ProductNotFoundException();

        CartItem cartItem = cart.addProduct(product);
        cartRepository.save(cart);
        CartItemDto cartItemDto = cartMapper.toDto(cartItem); //nullPointerException
        return cartItemDto;

    }

    public CartItemDto updateCartItem(UUID cartId, Long productId, Integer updated_quantity) {

        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart==null) throw new CartNotFoundException();

        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) throw new ProductNotFoundException();

        CartItem cartItem = cart.updateCartItem(product, updated_quantity);
        if (cartItem ==null) throw new CartItemNotFoundException();
        cartRepository.save(cart);

        CartItemDto cartItemDto = cartMapper.toDto(cartItem);
        return cartItemDto;
    }

    public void clearCart(UUID cartId) {
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();
        cart.clear();
        cartRepository.save(cart);
    }

    public void removeProduct(UUID cartId, Long productId) {
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart==null) throw new CartNotFoundException();

        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) throw new ProductNotFoundException();

        CartItem cartItem = cart.removeProduct(product);
        if (cartItem ==null) throw new CartItemNotFoundException();

        cartRepository.save(cart);
    }

}
