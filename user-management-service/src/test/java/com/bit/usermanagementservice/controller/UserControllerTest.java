package com.bit.usermanagementservice.controller;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;


    @Test
    public void testGetUser_WithValidId_ReturnsUserResponse() {
        // Arrange
        Long userId = 1L;
        UserResponse userResponse = UserResponse.builder().id(userId).username("testUser").email("test@domain.com").build();
        when(userService.getUser(userId)).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> responseEntity = userController.getUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userId, responseEntity.getBody().getId());
        assertEquals("testUser", responseEntity.getBody().getUsername());
        assertEquals("test@domain.com", responseEntity.getBody().getEmail());
    }

    @Test
    public void testGetAllUsers_ReturnsListOfUserResponses() {
        // Arrange
        List<UserResponse> userResponses = Arrays.asList(
                UserResponse.builder().id(1L).build(),
                UserResponse.builder().id(2L).build()
        );
        when(userService.getAllUsers()).thenReturn(userResponses);

        // Act
        ResponseEntity<List<UserResponse>> responseEntity = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals(1L, responseEntity.getBody().get(0).getId());
        assertEquals(2L, responseEntity.getBody().get(1).getId());
    }

    @Test
    public void testGetDeletedUsers_ReturnsListOfUserResponses() {
        // Arrange
        List<UserResponse> deletedUserResponses = Arrays.asList(
                UserResponse.builder().id(3L).build(),
                UserResponse.builder().id(4L).build()
        );
        when(userService.getDeletedUsers()).thenReturn(deletedUserResponses);

        // Act
        ResponseEntity<List<UserResponse>> responseEntity = userController.getDeletedUsers();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals(3L, responseEntity.getBody().get(0).getId());
        assertEquals(4L, responseEntity.getBody().get(1).getId());
    }

    @Test
    public void testGetAllUsersFilteredAndSorted_ReturnsPageOfUserResponses() {
        // Arrange
        Page<UserResponse> usersPage = new PageImpl<>(Arrays.asList(
                UserResponse.builder().id(5L).build(),
                UserResponse.builder().id(6L).build()
        ));
        when(userService.getAllUsersFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null)).thenReturn(usersPage);

        // Act
        ResponseEntity<Page<UserResponse>> responseEntity = userController.getAllUsersFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().getTotalElements());
        assertEquals(5L, responseEntity.getBody().getContent().get(0).getId());
        assertEquals(6L, responseEntity.getBody().getContent().get(1).getId());
    }

    @Test
    public void testCreateUser_WithValidUserRequest_ReturnsCreatedUserResponse() {
        // Arrange
        UserRequest userRequest = UserRequest.builder().username("newUser").email("new@domain.com").build();
        UserResponse userResponse = UserResponse.builder().id(7L).username("newUser").email("new@domain.com").build();
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> responseEntity = userController.createUser(userRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(7L, responseEntity.getBody().getId());
        assertEquals("newUser", responseEntity.getBody().getUsername());
        assertEquals("new@domain.com", responseEntity.getBody().getEmail());
    }

    @Test
    public void testUpdateUser_WithValidUserRequest_ReturnsUpdatedUserResponse() {
        // Arrange
        Long userId = 8L;
        UserRequest userRequest = UserRequest.builder().username("updatedUser").email("updated@domain.com").build();
        UserResponse userResponse = UserResponse.builder().id(userId).username("updatedUser").email("updated@domain.com").build();
        when(userService.updateUser(eq(userId), any(UserRequest.class))).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> responseEntity = userController.updateUser(userId, userRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userId, responseEntity.getBody().getId());
        assertEquals("updatedUser", responseEntity.getBody().getUsername());
        assertEquals("updated@domain.com", responseEntity.getBody().getEmail());
    }

    @Test
    public void testRestoreUser_WithValidId_ReturnsRestoredUserResponse() {
        // Arrange
        Long userId = 9L;
        UserResponse userResponse =  UserResponse.builder().id(userId).username("restoredUser").email("restored@domain.com").build();
        when(userService.restoreUser(userId)).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> responseEntity = userController.restoreUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userId, responseEntity.getBody().getId());
        assertEquals("restoredUser", responseEntity.getBody().getUsername());
        assertEquals("restored@domain.com", responseEntity.getBody().getEmail());
    }

    @Test
    public void testDeleteUser_WithValidId_ReturnsSuccessMessage() {
        // Arrange
        Long userId = 10L;

        // Act
        ResponseEntity<String> responseEntity = userController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User soft deleted successfully!", responseEntity.getBody());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    public void testDeleteUserPermanently_WithValidId_ReturnsSuccessMessage() {
        // Arrange
        Long userId = 11L;

        // Act
        ResponseEntity<String> responseEntity = userController.deleteUserPermanently(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User deleted permanently!", responseEntity.getBody());
        verify(userService, times(1)).deleteUserPermanently(userId);
    }
}
