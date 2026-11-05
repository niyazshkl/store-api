package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
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

        return addProductToNewCartItem(product);
        //cartItemRepostory.save() -> just use cascade = CascadeType.PERSIST
    }

    @NonNull
    private CartItem addProductToNewCartItem(Product product) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        this.items.add(cartItem);
        cartItem.setCart(this);
        return cartItem;
    }

    public CartItem updateCartItem(Product product, Integer quantity){
        CartItem item = getCartItemWithProduct(product);
        if (item==null) return null;
        item.setQuantity(quantity);
        return item;
    }

    private CartItem getCartItemWithProduct(Product product) {
        CartItem item = this.items.stream()
                .filter(cartItem ->
                   cartItem.getProduct().equals(product)
                ).findFirst().orElse(null);
        return item;
    }

    public CartItem removeProduct(Product product){
        CartItem item = getCartItemWithProduct(product);
        if (item==null) return null;
        items.remove(item);
        item.setCart(null);
        return item;
    }

    public void clear(){
        this.items.clear();
    }


}
