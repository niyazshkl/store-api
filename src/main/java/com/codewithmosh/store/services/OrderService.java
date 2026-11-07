package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.orders.OrderDto;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private  final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders(Long userId){
        User user = userRepository.getUserWithOrders(userId).orElseThrow(null);
        //should never be null or else accessed by protecrted endpoiunt
        List<OrderDto> orderDtos = new ArrayList<>();
        user.getOrders().forEach(order ->
            orderDtos.add(orderMapper.toDto(order))
        );
        return orderDtos;
    }
}
