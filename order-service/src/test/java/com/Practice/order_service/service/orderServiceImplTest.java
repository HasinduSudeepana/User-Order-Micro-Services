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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test for getOrderById method
    @Test
    public void testGetOrderById_Success() {
        // Arrange
        Long orderId = 1L;
        Orders mockOrder = new Orders();
        mockOrder.setOrderId(orderId);
        mockOrder.setUserId(100L);
        mockOrder.setProductName("Test Product");
        mockOrder.setPrice(BigDecimal.valueOf(1500.0));
        mockOrder.setQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(mockOrder));

        // Act & Assert
        StepVerifier.create(orderService.getOrderById(orderId))
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(orderId) &&
                                orderDTO.getUserId().equals(100L) &&
                                "Test Product".equals(orderDTO.getProductName()) &&
                                orderDTO.getPrice().equals(50.0) &&
                                orderDTO.getQuantity().equals(2)
                )
                .verifyComplete();
    }

    @Test
    public void testGetOrderById_NotFound() {
        // Arrange
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.getOrderById(orderId))
                .expectErrorMatches(throwable ->
                        throwable instanceof OrderNotFoundException &&
                                throwable.getMessage().equals("Order not found with this id 99")
                )
                .verify();
    }

    // Test for addOrder method
    @Test
    public void testAddOrder_Success() {
        // Arrange
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(100L);
        orderDTO.setProductName("New Product");
        orderDTO.setPrice(75.0);
        orderDTO.setQuantity(3);

        Orders savedOrder = new Orders();
        savedOrder.setOrderId(1L);
        savedOrder.setUserId(100L);
        savedOrder.setProductName("New Product");
        savedOrder.setPrice(75.0);
        savedOrder.setQuantity(3);

        when(orderRepository.save(any(Orders.class))).thenReturn(Mono.just(savedOrder));

        // Act & Assert
        StepVerifier.create(orderService.addOrder(orderDTO))
                .expectNextMatches(result -> {
                    OrderDTO resultDto = (OrderDTO) result;
                    return resultDto.getOrderId() != null &&
                            resultDto.getUserId().equals(100L) &&
                            "New Product".equals(resultDto.getProductName()) &&
                            resultDto.getPrice().equals(75.0) &&
                            resultDto.getQuantity().equals(3);
                })
                .verifyComplete();
    }

    // Test for getAllOrders method
    @Test
    public void testGetAllOrders_Success() {
        // Arrange
        Orders order1 = new Orders();
        order1.setOrderId(1L);
        order1.setUserId(100L);
        order1.setProductName("Product 1");
        order1.setPrice(50.0);
        order1.setQuantity(2);

        Orders order2 = new Orders();
        order2.setOrderId(2L);
        order2.setUserId(101L);
        order2.setProductName("Product 2");
        order2.setPrice(75.0);
        order2.setQuantity(3);

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));

        // Act & Assert
        StepVerifier.create(orderService.getAllOrders())
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(1L) &&
                                orderDTO.getUserId().equals(100L) &&
                                "Product 1".equals(orderDTO.getProductName())
                )
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(2L) &&
                                orderDTO.getUserId().equals(101L) &&
                                "Product 2".equals(orderDTO.getProductName())
                )
                .verifyComplete();
    }

    @Test
    public void testGetAllOrders_Empty() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.getAllOrders())
                .expectNextCount(0)
                .verifyComplete();
    }

    // Test for updateUser method
    @Test
    public void testUpdateOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Orders existingOrder = new Orders();
        existingOrder.setOrderId(orderId);
        existingOrder.setUserId(100L);
        existingOrder.setProductName("Old Product");
        existingOrder.setPrice(50.0);
        existingOrder.setQuantity(2);

        OrderDTO updatedOrderDTO = new OrderDTO();
        updatedOrderDTO.setUserId(101L);
        updatedOrderDTO.setProductName("Updated Product");
        updatedOrderDTO.setPrice(75.0);
        updatedOrderDTO.setQuantity(3);

        Orders updatedOrder = new Orders();
        updatedOrder.setOrderId(orderId);
        updatedOrder.setUserId(101L);
        updatedOrder.setProductName("Updated Product");
        updatedOrder.setPrice(75.0);
        updatedOrder.setQuantity(3);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenReturn(Mono.just(updatedOrder));

        // Act & Assert
        StepVerifier.create(orderService.updateUser(orderId, updatedOrderDTO))
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(orderId) &&
                                orderDTO.getUserId().equals(101L) &&
                                "Updated Product".equals(orderDTO.getProductName()) &&
                                orderDTO.getPrice().equals(75.0) &&
                                orderDTO.getQuantity().equals(3)
                )
                .verifyComplete();
    }

    @Test
    public void testUpdateOrder_NotFound() {
        // Arrange
        Long orderId = 99L;
        OrderDTO updatedOrderDTO = new OrderDTO();
        updatedOrderDTO.setUserId(101L);
        updatedOrderDTO.setProductName("Updated Product");
        updatedOrderDTO.setPrice(75.0);
        updatedOrderDTO.setQuantity(3);

        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.updateUser(orderId, updatedOrderDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof OrderNotFoundException &&
                                throwable.getMessage().equals("Order does not found with this id 99")
                )
                .verify();
    }

    // Test for deleteUser method
    @Test
    public void testDeleteOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Orders orderToDelete = new Orders();
        orderToDelete.setOrderId(orderId);
        orderToDelete.setUserId(100L);
        orderToDelete.setProductName("Product to Delete");
        orderToDelete.setPrice(50.0);
        orderToDelete.setQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(orderToDelete));

        // Act & Assert
        StepVerifier.create(orderService.deleteUser(orderId))
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(orderId) &&
                                orderDTO.getUserId().equals(100L) &&
                                "Product to Delete".equals(orderDTO.getProductName())
                )
                .verifyComplete();
    }

    @Test
    public void testDeleteOrder_NotFound() {
        // Arrange
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.deleteUser(orderId))
                .expectErrorMatches(throwable ->
                        throwable instanceof OrderNotFoundException &&
                                throwable.getMessage().equals("Order does not found with this id 99")
                )
                .verify();
    }

    // Test for getOrdersByUserId method
    @Test
    public void testGetOrdersByUserId_Success() {
        // Arrange
        Long userId = 100L;
        Orders order1 = new Orders();
        order1.setOrderId(1L);
        order1.setUserId(userId);
        order1.setProductName("Product 1");
        order1.setPrice(50.0);
        order1.setQuantity(2);

        Orders order2 = new Orders();
        order2.setOrderId(2L);
        order2.setUserId(userId);
        order2.setProductName("Product 2");
        order2.setPrice(75.0);
        order2.setQuantity(3);

        when(orderRepository.findByUserId(userId)).thenReturn(Flux.just(order1, order2));

        // Act & Assert
        StepVerifier.create(orderService.getOrdersByUserId(userId))
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(1L) &&
                                orderDTO.getUserId().equals(userId) &&
                                "Product 1".equals(orderDTO.getProductName())
                )
                .expectNextMatches(orderDTO ->
                        orderDTO.getOrderId().equals(2L) &&
                                orderDTO.getUserId().equals(userId) &&
                                "Product 2".equals(orderDTO.getProductName())
                )
                .verifyComplete();
    }

    @Test
    public void testGetOrdersByUserId_NoOrders() {
        // Arrange
        Long userId = 999L;
        when(orderRepository.findByUserId(userId)).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.getOrdersByUserId(userId))
                .expectNextCount(0)
                .verifyComplete();
    }
}