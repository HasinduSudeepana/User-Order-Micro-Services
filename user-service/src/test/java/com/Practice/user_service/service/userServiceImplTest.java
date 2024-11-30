package com.Practice.user_service.service;

import com.Practice.user_service.client.api.OrderApi;
import com.Practice.user_service.exception.UserAlreadyExistsException;
import com.Practice.user_service.exception.UserNotFoundException;
import com.Practice.user_service.model.User;
import com.Practice.user_service.repository.UserRepository;
import com.Practice.user_service.server.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class userServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private OrderApi orderApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserById_Success() {
        // Arrange: Create a mock User object and configure the mock repository
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUserName("kamlii");
        mockUser.setEmail("kamaliii@gmail.com");

        when(userRepository.findById(1L)).thenReturn(Mono.just(mockUser));

        // Act & Assert: Use StepVerifier to test the Mono
        StepVerifier.create(userService.getUserById(1L))
                .expectNextMatches(userDTO -> userDTO.getUserId() == 1L &&
                        "kamlii".equals(userDTO.getUserName()) &&
                        "kamaliii@gmail.com".equals(userDTO.getEmail()))
                .verifyComplete();
    }

    @Test
    public void testGetUserById_UserNotFound() {
        // Arrange: Mock the repository to return an empty Mono
        Long userId =99L; // Non-existent user ID for this test
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        // Act & Assert: Use StepVerifier to verify the exception
        StepVerifier.create(userService.getUserById(userId))
                .expectErrorMatches(throwable -> throwable instanceof UserNotFoundException &&
                        throwable.getMessage().equals("User not found with id: " + userId))
                .verify();
    }

    @Test
    public void testAddUser_Success() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("newUser");
        userDTO.setEmail("newuser@example.com");

        User savedUser = new User();
        savedUser.setUserId(1L);
        savedUser.setUserName("newUser");
        savedUser.setEmail("newuser@example.com");

        // Mock the repository to return empty when checking for existing email
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // Act & Assert
        StepVerifier.create(userService.addUser(userDTO))
                .expectNextMatches(result -> {
                    UserDTO resultDto = (UserDTO) result;
                    return resultDto.getUserId() != null &&
                            "newUser".equals(resultDto.getUserName()) &&
                            "newuser@example.com".equals(resultDto.getEmail());
                })
                .verifyComplete();
    }

    @Test
    public void testAddUser_UserAlreadyExists() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("existingUser");
        userDTO.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUserName("existingUser");
        existingUser.setEmail("existing@example.com");

        // Mock the repository to return an existing user
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Mono.just(existingUser));

        // Act & Assert
        StepVerifier.create(userService.addUser(userDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().equals("User with email existing@example.com already exists")
                )
                .verify();
    }

    @Test
    public void testGetAllUsers_Success() {
        // Arrange
        User user1 = new User();
        user1.setUserId(1L);
        user1.setUserName("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUserId(2L);
        user2.setUserName("user2");
        user2.setEmail("user2@example.com");

        // Mock the repository to return a Flux of users
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));

        // Act & Assert
        StepVerifier.create(userService.getAllUsers())
                .expectNextMatches(userDTO ->
                        userDTO.getUserId() == 1L &&
                                "user1".equals(userDTO.getUserName()) &&
                                "user1@example.com".equals(userDTO.getEmail())
                )
                .expectNextMatches(userDTO ->
                        userDTO.getUserId() == 2L &&
                                "user2".equals(userDTO.getUserName()) &&
                                "user2@example.com".equals(userDTO.getEmail())
                )
                .verifyComplete();
    }

    @Test
    public void testDeleteUser_Success() {
        // Arrange
        Long userId = 1L;
        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setUserName("userToDelete");
        userToDelete.setEmail("delete@example.com");

        // Mock the repository to return the user to be deleted
        when(userRepository.findById(userId)).thenReturn(Mono.just(userToDelete));

        // Act & Assert
        StepVerifier.create(userService.deleteUser(userId))
                .expectNextMatches(userDTO ->
                        userDTO.getUserId() == userId &&
                                "userToDelete".equals(userDTO.getUserName()) &&
                                "delete@example.com".equals(userDTO.getEmail())
                )
                .verifyComplete();
    }

    @Test
    public void testDeleteUser_UserNotFound() {
        // Arrange
        Long userId = 99L;

        // Mock the repository to return an empty Mono
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userService.deleteUser(userId))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found with this id 99")
                )
                .verify();
    }

    @Test
    public void testUpdateUser_Success() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUserName("oldName");
        existingUser.setEmail("old@example.com");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUserName("newName");
        updatedUserDTO.setEmail("new@example.com");

        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUserName("newName");
        updatedUser.setEmail("new@example.com");

        // Mock the repository to return the existing user and then save the updated user
        when(userRepository.findById(userId)).thenReturn(Mono.just(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        // Act & Assert
        StepVerifier.create(userService.updateUser(userId, updatedUserDTO))
                .expectNextMatches(userDTO ->
                        userDTO.getUserId() == userId &&
                                "newName".equals(userDTO.getUserName()) &&
                                "new@example.com".equals(userDTO.getEmail())
                )
                .verifyComplete();
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        // Arrange
        Long userId = 99L;
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUserName("newName");
        updatedUserDTO.setEmail("new@example.com");

        // Mock the repository to return an empty Mono
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userService.updateUser(userId, updatedUserDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found with this id 99")
                )
                .verify();
    }





}
