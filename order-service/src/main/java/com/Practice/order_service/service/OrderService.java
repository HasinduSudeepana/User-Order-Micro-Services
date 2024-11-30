package com.Practice.order_service.service;

import com.Practice.order_service.exception.OrderNotFoundException;
import com.Practice.order_service.model.Orders;
import com.Practice.order_service.repository.OrderRepository;
import com.Practice.order_service.server.model.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    @Autowired
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    //get Oder by id
    public Mono<OrderDTO> getOrderById(Long id){
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order not found with this id "+ id)))
                .map(this::toDTO);
    }

    //add order
    public Mono<?> addOrder(OrderDTO orderDTO){
        Orders orders = toEntity(orderDTO);
        return orderRepository.save(orders)
                .map(this::toDTO);
    }

    //getAllOrders
    public Flux<OrderDTO> getAllOrders(){
        return orderRepository.findAll()
                .map(this::toDTO);
    }

    //update order
    public Mono<OrderDTO> updateUser(Long id, OrderDTO updatedOrderDto){
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order does not found with this id "+ id)))
                .flatMap(existingOrder->{
                    existingOrder.setUserId(updatedOrderDto.getUserId());
                    existingOrder.setProductName(updatedOrderDto.getProductName());
                    existingOrder.setQuantity(updatedOrderDto.getQuantity());
                    existingOrder.setPrice(updatedOrderDto.getPrice());
                    return orderRepository.save(existingOrder);
                })
                .map(this::toDTO);
    }

    //delete order
    public Mono<OrderDTO> deleteUser(Long id){
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Order does not found with this id "+ id)))
                .map(this::toDTO);
    }

    //get orders by user id
    public Flux<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .switchIfEmpty(Flux.empty())
                .map(this::toDTO);
    }

    public OrderDTO toDTO(Orders orders){
        OrderDTO od = new OrderDTO();
        od.setOrderId(orders.getOrderId());
        od.setUserId(orders.getUserId());
        od.setProductName(orders.getProductName());
        od.setPrice(orders.getPrice());
        od.setQuantity(orders.getQuantity());
        return od;
    }

    public Orders toEntity(OrderDTO orderDTO){
        Orders orders = new Orders();
        orders.setOrderId(orderDTO.getOrderId());
        orders.setUserId(orderDTO.getUserId());
        orders.setProductName(orderDTO.getProductName());
        orders.setPrice(orderDTO.getPrice());
        orders.setQuantity(orderDTO.getQuantity());
        return orders;
    }
}
