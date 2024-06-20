package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.AuthUserRequest;
import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.entity.AppUser;
import com.bit.usermanagementservice.entity.Role;
import com.bit.usermanagementservice.exception.UserNotSoftDeletedException;
import com.bit.usermanagementservice.repository.RoleRepository;
import com.bit.usermanagementservice.repository.UserRepository;
import com.bit.usermanagementservice.wrapper.UpdateUserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        userService = spy(userService);
        lenient().doNothing().when(userService).validateRoles(anyList());
    }

    @Test
    void testGetUser_WithValidId_ReturnsUserResponse() {
        // Arrange
        Long userId = 1L;
        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername("testUser");
        user.setEmail("test@domain.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse userResponse = userService.getUser(userId);

        // Assert
        assertNotNull(userResponse);
        assertEquals(userId, userResponse.getId());
        assertEquals("testUser", userResponse.getUsername());
        assertEquals("test@domain.com", userResponse.getEmail());
    }

    @Test
    void testGetAllUsers_ReturnsListOfUserResponses() {
        // Arrange
        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@domain.com");

        AppUser user2 = new AppUser();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@domain.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserResponse> userResponses = userService.getAllUsers();

        // Assert
        assertNotNull(userResponses);
        assertEquals(2, userResponses.size());
        assertEquals(1L, userResponses.get(0).getId());
        assertEquals(2L, userResponses.get(1).getId());
    }

    @Test
    void testGetDeletedUsers_ReturnsListOfUserResponses() {
        // Arrange
        AppUser deletedUser1 = new AppUser();
        deletedUser1.setId(3L);
        deletedUser1.setUsername("deletedUser1");
        deletedUser1.setEmail("deleted1@domain.com");

        AppUser deletedUser2 = new AppUser();
        deletedUser2.setId(4L);
        deletedUser2.setUsername("deletedUser2");
        deletedUser2.setEmail("deleted2@domain.com");

        when(userRepository.findSoftDeletedUsers()).thenReturn(Arrays.asList(deletedUser1, deletedUser2));

        // Act
        List<UserResponse> deletedUserResponses = userService.getDeletedUsers();

        // Assert
        assertNotNull(deletedUserResponses);
        assertEquals(2, deletedUserResponses.size());
        assertEquals(3L, deletedUserResponses.get(0).getId());
        assertEquals(4L, deletedUserResponses.get(1).getId());
    }

    @Test
    void testGetAllUsersFilteredAndSorted_ReturnsPageOfUserResponses() {
        // Arrange
        AppUser user1 = new AppUser();
        user1.setId(5L);
        user1.setUsername("sortedUser1");
        user1.setEmail("sorted1@domain.com");

        AppUser user2 = new AppUser();
        user2.setId(6L);
        user2.setUsername("sortedUser2");
        user2.setEmail("sorted2@domain.com");

        Page<AppUser> usersPage = new PageImpl<>(Arrays.asList(user1, user2));
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);

        // Act
        Page<UserResponse> resultPage = userService.getAllUsersFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null);

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(5L, resultPage.getContent().get(0).getId());
        assertEquals(6L, resultPage.getContent().get(1).getId());
    }

    @Test
    void testCreateUser_WithValidUserRequest_ReturnsCreatedUserResponse() {
        // Arrange
        UserRequest userRequest = UserRequest.builder().username("newUser").email("new@domain.com").password("password123").build();
        Role role = new Role();
        role.setRoleName("USER");
        userRequest.setRoles(List.of("USER"));
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));

        AppUser user = new AppUser();
        user.setId(7L);
        user.setUsername("newUser");
        user.setEmail("new@domain.com");

        // Act
        UserResponse userResponse = userService.createUser(userRequest);

        // Assert
        assertNotNull(userResponse);
        assertEquals("newUser", userResponse.getUsername());
        assertEquals("new@domain.com", userResponse.getEmail());
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(AuthUserRequest.class));
    }

    @Test
    void testUpdateUser_WithValidUserRequest_ReturnsUpdatedUserResponse() {
        // Arrange
        Long userId = 8L;
        UserRequest userRequest = UserRequest.builder().username("updatedUser").email("updated@domain.com").password("password123").build();
        Role role = new Role();
        role.setRoleName("USER");
        userRequest.setRoles(List.of("USER"));
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));

        AppUser existingUser = new AppUser();
        existingUser.setId(userId);
        existingUser.setUsername("oldUser");
        existingUser.setEmail("old@domain.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenReturn(existingUser);

        // Act
        UserResponse userResponse = userService.updateUser(userId, userRequest);

        // Assert
        assertNotNull(userResponse);
        assertEquals("updatedUser", userResponse.getUsername());
        assertEquals("updated@domain.com", userResponse.getEmail());
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(UpdateUserMessage.class));
    }

    @Test
    void testRestoreUser_WithValidId_ReturnsRestoredUserResponse() {
        // Arrange
        Long userId = 9L;
        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername("restoredUser");
        user.setEmail("restored@domain.com");

        when(userRepository.existsByIdAndDeletedTrue(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse userResponse = userService.restoreUser(userId);

        // Assert
        assertNotNull(userResponse);
        assertEquals(userId, userResponse.getId());
        assertEquals("restoredUser", userResponse.getUsername());
        assertEquals("restored@domain.com", userResponse.getEmail());
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), eq(userId));
    }

    @Test
    void testRestoreUser_NonExistingSoftDeletedUser_ThrowsUserNotSoftDeletedException() {
        // Arrange
        Long id = 1L;
        AppUser existingUser = new AppUser();
        lenient().when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(UserNotSoftDeletedException.class, () -> userService.restoreUser(id));
    }

    @Test
    void testDeleteUser_WithValidId_SendsDeleteMessage() {
        // Arrange
        Long userId = 10L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), eq(userId));
    }

    @Test
    void testDeleteUserPermanently_WithValidId_SendsDeletePermanentMessage() {
        // Arrange
        Long userId = 11L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUserPermanently(userId);

        // Assert
        verify(userRepository, times(1)).deleteRolesForUser(userId);
        verify(userRepository, times(1)).deletePermanently(userId);
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), eq(userId));
    }
}
