package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "carts")
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id")
    private UUID id;

    @Column(name = "date_created", insertable = false, updatable = false)
    private Date dateCreated;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.MERGE)
    private Set<CartItem> items = new HashSet<>();

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getCartItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CartItem addProduct(Product product){
        for (CartItem cartItem: items) {
            if (cartItem.getProduct().getId()==product.getId()){
                cartItem.setQuantity(cartItem.getQuantity()+1);
                return cartItem;
            }
        }

//        //check if product already exists in the cart items,
//        AtomicBoolean found = new AtomicBoolean(false);
//        cartItems.stream().forEach(cartItem -> {
//            //if so then update the count, or else add it
//            if (cartItem.getProduct().getId()== product.getId()){
//                cartItem.setQuantity(cartItem.getQuantity()+1);
//                found.set(true);
//            }
//        });
//        if (found.get()) return;

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        this.items.add(cartItem);
        cartItem.setCart(this);
        return cartItem;
        //cartItemRepostory.save() -> just use cascade = CascadeType.PERSIST
    }
}
