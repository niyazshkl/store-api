package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CheckoutResponseDto;
import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;


    @Transactional
    public CheckoutResponseDto checkout(UUID cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(CartNotFoundException::new);
        Set<CartItem> cartItems = cart.getItems();
        if (cartItems.isEmpty()) throw new CartEmptyException();

        /*
        Get the cart items -> create order items
        create an order with those items
        clear the cart
        return orderId
         */

        Order order = Order.fromCart(cart);

        Long userId =  Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        User user = userRepository.findById(userId).orElse(null); //should never be null by protected endpoint

        order.setCustomer(user);

        orderRepository.save(order);

        /*
        Create checkout stripe session (specify success and fail url)
            - Need to create a session?
                - Need session creation parameters (params)
                    - mode (Payment, Subscription, etc)
                    - successUrl (where?)
                    - cancelUrl (where?)
                    - LineItems (what items to show on the stripe checkout session page?)
                        - Quantity
                        - PriceData
                            - currency
                            - unitAmount (price per product)
                            - ProductData
                                - name (what is the name of the product?)
                                - description (optional)

        Redirect user there
        Whatever happens is managed by stripe checkout (it will direct the user to either success or fail)
        Finally, it will send a webhook event to let us know in the server backend what happened so we can handle that.
         */

        /*
        build product data (name/description) -> build price data (currency/amount)
      ->build lineItem(quantity) -> createSession(mode,success,cancel)
         */

        try{

            // Create a checkout session
            CheckoutSession session = paymentGateway.createCheckoutSession(order);

            cart.clear();
            cartRepository.save(cart);

            return new CheckoutResponseDto(order.getId(), session.getCheckoutUrl());

        } catch (PaymentException ex) {
            orderRepository.delete(order);
            throw ex;
        }
    }

    public void handleWebhookEvent(Map<String,String> headers, String payload) throws PaymentException{
        PaymentResult paymentResult = paymentGateway.parseWebhookRequest(headers, payload).orElse(null);
        if (paymentResult.getPaymentStatus()==PaymentStatus.PAYMENT_INTENT_SUCCEEDED) setOrderStatus(Long.valueOf(paymentResult.getOrderId()), OrderStatus.PAID);
        else setOrderStatus(Long.valueOf(paymentResult.getOrderId()), OrderStatus.FAILED);
    }
    private void setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order =  orderRepository.findById(orderId).orElse(null);
        order.setStatus(orderStatus);
        orderRepository.save(order);
    }
}
