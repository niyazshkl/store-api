package com.codewithmosh.store.controllers;

import com.codewithmosh.store.orders.OrderDto;
import com.codewithmosh.store.orders.OrderNotFound;
import com.codewithmosh.store.orders.UserDoesNotOwnOrder;
import com.codewithmosh.store.orders.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        List<OrderDto> orders = orderService.getAllOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("{orderId}")
    public ResponseEntity<OrderDto> getOrderForUser(@PathVariable(name = "orderId") Long orderId) throws Exception{
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        OrderDto orderDto = orderService.getOrderForUser(orderId, userId);
        return ResponseEntity.ok(orderDto);
    }

    @ExceptionHandler(UserDoesNotOwnOrder.class)
    public ResponseEntity<Map<String,String>> handleUserDoesNotOwnOrder(UserDoesNotOwnOrder exception){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("Error", "User does not own order")
        );
    }
    @ExceptionHandler(OrderNotFound.class)
    public ResponseEntity<Map<String,String>> handleOrderNotFound(OrderNotFound exception){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("Error", "Order not found")
        );
    }
}
