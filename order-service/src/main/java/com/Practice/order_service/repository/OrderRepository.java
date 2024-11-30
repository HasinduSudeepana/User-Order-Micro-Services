package com.Practice.order_service.repository;

import com.Practice.order_service.model.Orders;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Orders,Long> {

    Flux<Orders> findByUserId(Long userId);
}
