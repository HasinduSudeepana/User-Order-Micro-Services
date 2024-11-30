package com.Practice.order_service.exception;

import com.Practice.order_service.server.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Response> handleOrderNotFoundException(OrderNotFoundException orderNotFoundException){
        Response response = new Response();
        response.setResponseCode("ORDER_NOT_FOUND");
        response.setMessage(orderNotFoundException.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return Mono.just(response);
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<Response> handleOrderAlreadyExistsException(OrderAlreadyExistsException orderAlreadyExistsException){
        Response response = new Response();
        response.setResponseCode("ORDER_ALREADY_EXISTS");
        response.setMessage(orderAlreadyExistsException.getMessage());
        response.setTimestamp(response.getTimestamp());
        return Mono.just(response);
    }
}
