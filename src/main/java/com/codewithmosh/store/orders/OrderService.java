package com.codewithmosh.store.orders;

import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private  final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    public List<OrderDto> getAllOrders(Long userId){
        User user = userRepository.getUserWithOrders(userId).orElseThrow(null);
        //should never be null or else accessed by protecrted endpoiunt
        List<OrderDto> orderDtos = new ArrayList<>();
        user.getOrders().forEach(order ->
            orderDtos.add(orderMapper.toDto(order))
        );
        return orderDtos;
    }
    public OrderDto getOrderForUser(Long orderId, Long userId) throws Exception{
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFound::new);
        User user = userRepository.getUserWithOrders(userId).orElseThrow(null);
        //should never be null or else accessed by unprotected endpoint
        Order userOrder = user.getOrders().stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(UserDoesNotOwnOrder::new);
        return orderMapper.toDto(userOrder);
    }
}
