package com.Practice.order_service.api;

import com.Practice.order_service.server.api.OrdersApiDelegate;
import com.Practice.order_service.server.model.OrderDTO;
import com.Practice.order_service.server.model.Response;
import com.Practice.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OrdersApiImpl implements OrdersApiDelegate {
    @Autowired
    private final OrderService orderService;


    public OrdersApiImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Mono<ResponseEntity<OrderDTO>> getOrderById(Long id, ServerWebExchange exchange) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Response>> addOrder(Mono<OrderDTO> orderDTO, ServerWebExchange exchange) {
        return orderDTO
                .flatMap(orderService::addOrder)
                .map(order->{
                    Response response = new Response();
                    response.setResponseCode("ORDER_ADDED_SUCCESSFULLy");
                    response.setMessage("Order has been added successfully");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e-> Mono.fromSupplier(()->{
                    Response response = new Response();
                    response.setMessage(e.getMessage());
                    response.setResponseCode("ORDER_ADDED_FAILED");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }));

    }

    @Override
    public Mono<ResponseEntity<Flux<OrderDTO>>> getAllOrders(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(orderService.getAllOrders()));
    }

    @Override
    public Mono<ResponseEntity<Response>> updateOrder(Long id, Mono<OrderDTO> orderDTO, ServerWebExchange exchange) {
        return orderDTO
                .flatMap(updatedDto-> orderService.updateUser(id, updatedDto))
                .then(Mono.fromSupplier(()-> {
                    Response response = new Response();
                    response.setResponseCode("ORDER_UPDATED_SUCCESSFULLY");
                    response.setMessage("Order has been updated successfully");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e-> Mono.fromSupplier(()->{
                    Response response = new Response();
                    response.setResponseCode("ORDER_UPDATE_FAIL");
                    response.setMessage(e.getMessage());
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }

    @Override
    public Mono<ResponseEntity<Response>> deleteOrder(Long id, ServerWebExchange exchange) {
        return orderService.deleteUser(id)
                .then(Mono.fromSupplier(()->{
                    Response response = new Response();
                    response.setResponseCode("ORDER_DELETED_SUCCESSFULLY");
                    response.setMessage("Order has been deleted successfully");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Response response = new Response();
                    response.setMessage(e.getMessage());
                    response.setResponseCode("ORDER_DELETION_FAILED");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }

    @Override
    public Mono<ResponseEntity<Flux<OrderDTO>>> getOrdersByUserId(Long id, ServerWebExchange exchange) {
        return Mono.just(
                ResponseEntity.ok(orderService.getOrdersByUserId(id))
        );
    }

}
