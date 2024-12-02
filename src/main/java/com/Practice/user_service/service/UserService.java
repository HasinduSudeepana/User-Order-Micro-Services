package com.Practice.user_service.service;

import com.Practice.user_service.client.api.OrderApi;
import com.Practice.user_service.exception.UserAlreadyExistsException;
import com.Practice.user_service.exception.UserNotFoundException;
import com.Practice.user_service.model.User;
import com.Practice.user_service.repository.UserRepository;
import com.Practice.user_service.server.model.UserDTO;
import com.Practice.user_service.server.model.UserOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    private final OrderApi orderApi;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.orderApi = new OrderApi();
    }

    //Get user by id
    public Mono<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)))
                .map(this::toDTO);
    }


    //add user
    public Mono<?> addUser(UserDTO userDTO) {
        return userRepository.findByEmail(userDTO.getEmail())
                .flatMap(existingUser -> Mono.error(new UserAlreadyExistsException(
                        "User with email " + userDTO.getEmail() + " already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = toEntity(userDTO);
                    return userRepository.save(user)
                            .map(this::toDTO);
                }));
    }

    //get all users
    public Flux<UserDTO> getAllUsers(){
        return userRepository.findAll()
                .switchIfEmpty(Mono.error(new UserNotFoundException("No users found in your table")))
                .map(this::toDTO);
    }

    //update user
    public Mono<UserDTO> updateUser(Long id, UserDTO updatedUserDTO) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with this id "+ id)))
                .flatMap(existingUser -> {
                    existingUser.setUserName(updatedUserDTO.getUserName());
                    existingUser.setEmail(updatedUserDTO.getEmail());
                    return userRepository.save(existingUser);
                })
                .map(this::toDTO);
    }


    //delete user by id
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with this id " + id)))
                .flatMap(existingUser -> userRepository.deleteById(id));
    }


    //get user details and own order details
    public Mono<UserOrderResponse> getUserWithOrders(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + id)))
                .flatMap(user -> {
                    UserDTO userDTO = toDTO(user);
                    return orderApi.getOrdersByUserId(id)
                            .map(this::mapToServerOrderDTO)
                            .collectList()
                            .map(orderList -> {
                                UserOrderResponse response = new UserOrderResponse();
                                response.setUser(userDTO);
                                response.setOrders(orderList);
                                return response;
                            });
                });
    }



    public UserDTO toDTO(User user) {
        UserDTO ud = new UserDTO();
        ud.setUserId(user.getUserId());
        ud.setUserName(user.getUserName());
        ud.setEmail(user.getEmail());
        return ud;
    }

    public User toEntity(UserDTO ud) {
        User user = new User();
        user.setUserName(ud.getUserName());
        user.setEmail(ud.getEmail());
        return user;
    }

    private com.Practice.user_service.server.model.OrderDTO mapToServerOrderDTO(com.Practice.user_service.client.model.OrderDTO clientOrderDTO) {

        com.Practice.user_service.server.model.OrderDTO serverOrderDTO = new com.Practice.user_service.server.model.OrderDTO();

        serverOrderDTO.setOrderId(clientOrderDTO.getOrderId());
        serverOrderDTO.setUserId(clientOrderDTO.getUserId());
        serverOrderDTO.setProductName(clientOrderDTO.getProductName());
        serverOrderDTO.setPrice(clientOrderDTO.getPrice());
        serverOrderDTO.setQuantity(clientOrderDTO.getQuantity());

        return serverOrderDTO;
    }


}