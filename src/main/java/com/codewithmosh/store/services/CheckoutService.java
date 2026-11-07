package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public Long checkout(UUID cartId) throws Exception{
        Cart cart = cartRepository.findById(cartId).orElseThrow(CartNotFoundException::new);
        Set<CartItem> cartItems = cart.getItems();
        if (cartItems.isEmpty()) throw new CartEmptyException();

        /*
        Get the cart items -> create order items
        create an order with those items
        clear the cart
        return orderId
         */

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        Set<OrderItem> orderItems = new HashSet<OrderItem>();

        for (CartItem cartItem: cart.getItems()){
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.calculateTotalPrice();
            orderItems.add(orderItem);
        }

        orderItems.stream().forEach(orderItem -> order.addOrderItem(orderItem));
        order.calculteTotalPrice();
        Long userId =  Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        User user = userRepository.findById(userId).orElse(null); //should never be null by protected endpoint
        user.addOrder(order);
        orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);

        return order.getId();
    }
}
