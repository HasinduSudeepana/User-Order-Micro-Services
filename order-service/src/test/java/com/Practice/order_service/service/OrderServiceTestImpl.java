package com.Practice.order_service.service;

import com.Practice.order_service.exception.OrderNotFoundException;
import com.Practice.order_service.model.Orders;
import com.Practice.order_service.repository.OrderRepository;
import com.Practice.order_service.server.model.OrderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class OrderServiceTestImpl {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService; // Real instance of OrderService with mocked OrderRepository

    private Orders sampleOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleOrder = new Orders();
        sampleOrder.setOrderId(1L);
        sampleOrder.setUserId(101L);
        sampleOrder.setProductName("Laptop");
        sampleOrder.setPrice(new BigDecimal("1000.00"));
        sampleOrder.setQuantity(2);
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(sampleOrder));

        StepVerifier.create(orderService.getOrderById(1L))
                .expectNextMatches(orderDTO -> orderDTO.getOrderId().equals(1L)
                        && orderDTO.getProductName().equals("Laptop"))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_Failure() {
        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderById(1L))
                .expectErrorMatches(throwable -> throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Order not found with this id 1"))
                .verify();

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testAddOrder_Success() {
        when(orderRepository.save(any(Orders.class))).thenReturn(Mono.just(sampleOrder));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setUserId(101L);
        orderDTO.setProductName("Laptop");
        orderDTO.setPrice(new BigDecimal("1000.00"));
        orderDTO.setQuantity(2);

        StepVerifier.create(orderService.addOrder(orderDTO))
                .expectNextMatches(savedOrder -> savedOrder instanceof OrderDTO &&
                        ((OrderDTO) savedOrder).getOrderId().equals(1L))
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Orders.class));
    }

    @Test
    void testGetAllOrders_Success() {
        when(orderRepository.findAll()).thenReturn(Flux.just(sampleOrder));

        StepVerifier.create(orderService.getAllOrders())
                .expectNextMatches(orderDTO -> orderDTO.getOrderId().equals(1L)
                        && orderDTO.getProductName().equals("Laptop"))
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetAllOrders_Empty() {
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllOrders())
                .expectErrorMatches(throwable -> throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Orders not found in your table"))
                .verify();

        verify(orderRepository, times(1)).findAll();
    }


    @Test
    void testUpdateOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(sampleOrder));
        when(orderRepository.save(any(Orders.class))).thenReturn(Mono.just(sampleOrder));

        OrderDTO updatedOrderDTO = new OrderDTO();
        updatedOrderDTO.setUserId(102L);
        updatedOrderDTO.setProductName("Tablet");
        updatedOrderDTO.setPrice(new BigDecimal("800.00"));
        updatedOrderDTO.setQuantity(1);

        StepVerifier.create(orderService.updateUser(1L, updatedOrderDTO))
                .expectNextMatches(orderDTO -> orderDTO.getProductName().equals("Tablet")
                        && orderDTO.getPrice().compareTo(new BigDecimal("800.00")) == 0)
                .verifyComplete();

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Orders.class));
    }

    @Test
    void testUpdateOrder_Failure() {
        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        OrderDTO updatedOrderDTO = new OrderDTO();

        StepVerifier.create(orderService.updateUser(1L, updatedOrderDTO))
                .expectErrorMatches(throwable -> throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Order does not found with this id 1"))
                .verify();

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Orders.class));
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(sampleOrder));
        when(orderRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.deleteUser(1L))
                .verifyComplete();

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteOrder_Failure() {
        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.deleteUser(1L))
                .expectErrorMatches(throwable -> throwable instanceof OrderNotFoundException &&
                        throwable.getMessage().equals("Order does not found with this id 1"))
                .verify();

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).deleteById(1L);
    }

    @Test
    void testGetOrdersByUserId_Success() {
        when(orderRepository.findByUserId(101L)).thenReturn(Flux.just(sampleOrder));

        StepVerifier.create(orderService.getOrdersByUserId(101L))
                .expectNextMatches(orderDTO -> orderDTO.getUserId().equals(101L)
                        && orderDTO.getProductName().equals("Laptop"))
                .verifyComplete();

        verify(orderRepository, times(1)).findByUserId(101L);
    }

    @Test
    void testGetOrdersByUserId_Empty() {
        when(orderRepository.findByUserId(101L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrdersByUserId(101L))
                .verifyComplete(); // Expecting no elements in Flux

        verify(orderRepository, times(1)).findByUserId(101L);
    }
}
