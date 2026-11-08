package com.codewithmosh.store.users;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "orders")
    @Query(value = "SELECT u FROM User u WHERE u.id = :userId", nativeQuery = false)
    Optional<User> getUserWithOrders(@Param("userId") Long userId);
}
