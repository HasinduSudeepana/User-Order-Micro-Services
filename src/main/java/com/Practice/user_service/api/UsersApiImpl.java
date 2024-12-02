package com.Practice.user_service.api;

import com.Practice.user_service.server.api.UsersApiDelegate;
import com.Practice.user_service.server.model.Response;
import com.Practice.user_service.server.model.UserDTO;
import com.Practice.user_service.server.model.UserOrderResponse;
import com.Practice.user_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UsersApiImpl implements UsersApiDelegate {

    private final UserService userService;

    public UsersApiImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<ResponseEntity<UserDTO>> getUserById(Long id, ServerWebExchange exchange) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<Response>> addUser(Mono<UserDTO> userDTO, ServerWebExchange exchange) {
        return userDTO.flatMap(userService::addUser)
                .map(user -> {
                    Response response = new Response();
                    response.setMessage("User has been added successfully.");
                    response.setResponseCode("USER_ADDED_SUCCESSFULLY");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    Response response = new Response();
                    response.setMessage(e.getMessage());
                    response.setResponseCode("USER_ADDED_FAILED");
                    response.setTimestamp(LocalDateTime.now());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
                });

    }


    @Override
    public Mono<ResponseEntity<Flux<UserDTO>>> getAllUsers(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(userService.getAllUsers()));
    }

    @Override
    public Mono<ResponseEntity<Response>> updateUser(Long id, Mono<UserDTO> userDTO, ServerWebExchange exchange) {
        return userDTO
                .flatMap(dto -> userService.updateUser(id, dto))
                .then(Mono.fromSupplier(() -> {
                    Response response = new Response();
                    response.setMessage("User has been updated successfully.");
                    response.setResponseCode("USER_UPDATED_SUCCESSFULLY");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Response response = new Response();
                    response.setMessage(e.getMessage());
                    response.setResponseCode("USER_UPDATE_FAILED");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }

    @Override
    public Mono<ResponseEntity<Response>> deleteUser(Long id, ServerWebExchange exchange) {
        return userService.deleteUser(id)
                .then(Mono.fromSupplier(() -> {
                    Response response = new Response();
                    response.setMessage("User has been deleted successfully.");
                    response.setResponseCode("USER_DELETED_SUCCESSFULLY");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(e -> Mono.fromSupplier(() -> {
                    Response response = new Response();
                    response.setMessage(e.getMessage());
                    response.setResponseCode("USER_DELETION_FAILED");
                    response.setTimestamp(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }


    @Override
    public Mono<ResponseEntity<UserOrderResponse>> getUserWithOrderDetails(Long id, ServerWebExchange exchange) {
        return userService.getUserWithOrders(id)
                .map(ResponseEntity::ok);
    }

}
