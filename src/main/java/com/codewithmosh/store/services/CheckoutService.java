package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CheckoutResponseDto;
import com.codewithmosh.store.entities.*;
import com.codewithmosh.store.exceptions.cart.CartEmptyException;
import com.codewithmosh.store.exceptions.cart.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${website.url}")
    private String websiteUrl;

    @Transactional
    public CheckoutResponseDto checkout(UUID cartId) throws Exception{
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

        user.addOrder(order);

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


//
            // Create a checkout session
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel");

            order.getItems().forEach(item -> {
                var lineItem = SessionCreateParams.LineItem.builder()
                        .setQuantity(Long.valueOf(item.getQuantity()))
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("usd")
                                        .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(item.getProduct().getName())
                                                        .build()
                                        )
                                        .build()
                        ).build();
                builder.addLineItem(lineItem);
            });

            var session = Session.create(builder.build());
            String checkoutUrl = session.getUrl();

            orderRepository.save(order);

            cart.clear();
            cartRepository.save(cart);

            return new CheckoutResponseDto(order.getId(), checkoutUrl);
        } catch (StripeException ex) {
            orderRepository.delete(order);
            ex.printStackTrace();
            throw ex;
        }
    }
}
