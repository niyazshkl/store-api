package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends CrudRepository<Cart, UUID> {
    @EntityGraph(attributePaths = "items.product")
    @Query(value = "SELECT c FROM Cart c WHERE c.id = :id", nativeQuery = false) //must use JPQL to use entity EntityGraph
    Optional<Cart> getCartWithItems(@Param("id") UUID id);
}
