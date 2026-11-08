package com.codewithmosh.store.entities;

import com.codewithmosh.store.carts.Cart;
import com.codewithmosh.store.carts.CartItem;
import com.codewithmosh.store.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    public void setCustomer(User customer) {
        this.customer = customer;
        customer.addOrder(this);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING) //store string in db
    private OrderStatus status;

    @Column(name = "created_at", updatable = false ,insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    public static Order fromCart(Cart cart){

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        for (CartItem cartItem: cart.getItems()){
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.calculateTotalPrice();
            order.addOrderItem(orderItem);
        }

        order.calculteTotalPrice();

        return order;
    }

    public void addOrderItem(OrderItem orderItem){
        this.items.add(orderItem);
        orderItem.setOrder(this);
    }

    public void calculteTotalPrice(){
        items.stream().forEach(
                orderItem -> {
                    BigDecimal orderItemTotalPrice = orderItem.getTotalPrice();
                    totalPrice = totalPrice.add(orderItemTotalPrice);
//                    totalPrice.add(orderItem.getTotalPrice())
                }
        );
    }
}
