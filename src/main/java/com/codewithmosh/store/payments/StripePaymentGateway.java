package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.Order;
import com.codewithmosh.store.orders.OrderItem;
import com.codewithmosh.store.orders.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway{

    private final OrderRepository orderRepository;

    @Value("${website.url}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        try {
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .putMetadata("orderId", order.getId().toString());

            order.getItems().forEach(item -> {
                var lineItem = createLineItem(item);
                builder.addLineItem(lineItem);
            });

            var session = Session.create(builder.build());
            return new CheckoutSession(session.getUrl());
        } catch (StripeException e) {
            //We should log this for a more advanced project
            throw new PaymentException();
        }
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(Map<String, String> headers, String payload) throws PaymentException {
        try {

            String signature = headers.get("stripe-signature");

            var event = Webhook.constructEvent(payload,signature,webhookSecretKey);

            var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

            switch (event.getType()){
                case "payment_intent.succeeded" -> {
                    //update order status to PAID
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    if (paymentIntent!=null){
                        String orderId = paymentIntent.getMetadata().get("orderId");
                        PaymentResult paymentResult = new PaymentResult(Long.valueOf(orderId), PaymentStatus.PAYMENT_INTENT_SUCCEEDED);
                        return Optional.of(paymentResult);
                    }
                }
                case "payment_intent.payment_failed" -> {
                    //update order status to FAILED
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    if (paymentIntent!=null){
                        String orderId = paymentIntent.getMetadata().get("orderId");
                        PaymentResult paymentResult = new PaymentResult(Long.valueOf(orderId), PaymentStatus.PAYMENT_INTENT_PAYMENT_FAILED);
                        return Optional.of(paymentResult);
                    }
                }
            }

        } catch (SignatureVerificationException e) {
            throw new PaymentException();
        }
        return Optional.empty();
    }



    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(
                        createPriceData(item)
                ).build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(
                        createProductData(item)
                )
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getProduct().getName())
                .build();
    }
}
