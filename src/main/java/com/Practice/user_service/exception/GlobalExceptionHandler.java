package com.Practice.user_service.exception;

import com.Practice.user_service.server.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;


@RestControllerAdvice    //global exception handler for rest controllers
public class GlobalExceptionHandler {
    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Response> handleUserNotFoundException(UserNotFoundException ex) {
        Response response = new Response();
        response.setResponseCode("USER_NOT_FOUND");
        response.setMessage(ex.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return Mono.just(response);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<Response> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Response response = new Response();
        response.setResponseCode("USER_ALREADY_EXISTS");
        response.setMessage(ex.getMessage());
        response.setTimestamp(LocalDateTime.now());
        return Mono.just(response);
    }


}