package com.bit.usermanagementservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetUser_WithValidId_ReturnsUserResponse() throws Exception {
        Long userId = 1L;
        UserResponse userResponse = UserResponse.builder().id(userId).name("testUser").email("test@domain.com").build();

        when(userService.getUser(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@domain.com"));
    }

    @Test
    public void testGetAllUsers_ReturnsListOfUserResponses() throws Exception {
        List<UserResponse> userResponses = Arrays.asList(
                UserResponse.builder().id(1L).build(),
                UserResponse.builder().id(2L).build()
        );
        when(userService.getAllUsers()).thenReturn(userResponses);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void testGetDeletedUsers_ReturnsListOfUserResponses() throws Exception {
        List<UserResponse> deletedUserResponses = Arrays.asList(
                UserResponse.builder().id(3L).build(),
                UserResponse.builder().id(4L).build()
        );
        when(userService.getDeletedUsers()).thenReturn(deletedUserResponses);

        mockMvc.perform(get("/users/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[1].id").value(4L));
    }

    @Test
    public void testGetAllUsersFilteredAndSorted_ReturnsPageOfUserResponses() throws Exception {
        Page<UserResponse> usersPage = new PageImpl<>(Arrays.asList(
                UserResponse.builder().id(5L).build(),
                UserResponse.builder().id(6L).build()
        ));
        when(userService.getAllUsersFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null)).thenReturn(usersPage);

        mockMvc.perform(get("/users/filteredAndSorted")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[1].id").value(6L));
    }

    @Test
    public void testCreateUser_WithValidUserRequest_ReturnsCreatedUserResponse() throws Exception {
        UserRequest userRequest = UserRequest.builder().username("newUser").email("new@domain.com").build();
        UserResponse userResponse = UserResponse.builder().id(7L).username("newUser").email("new@domain.com").build();
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("new@domain.com"));
    }

    @Test
    public void testUpdateUser_WithValidUserRequest_ReturnsUpdatedUserResponse() throws Exception {
        Long userId = 8L;
        UserRequest userRequest = UserRequest.builder().username("updatedUser").email("updated@domain.com").build();;
        UserResponse userResponse = UserResponse.builder().id(userId).username("updatedUser").email("updated@domain.com").build();
        when(userService.updateUser(eq(userId), any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.email").value("updated@domain.com"));
    }

    @Test
    public void testRestoreUser_WithValidId_ReturnsRestoredUserResponse() throws Exception {
        Long userId = 9L;
        UserResponse userResponse =  UserResponse.builder().id(userId).username("restoredUser").email("restored@domain.com").build();
        when(userService.restoreUser(userId)).thenReturn(userResponse);

        mockMvc.perform(put("/users/restore/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("restoredUser"))
                .andExpect(jsonPath("$.email").value("restored@domain.com"));
    }

    @Test
    public void testDeleteUser_WithValidId_ReturnsSuccessMessage() throws Exception {
        Long userId = 10L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User soft deleted successfully!"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    public void testDeleteUserPermanently_WithValidId_ReturnsSuccessMessage() throws Exception {
        Long userId = 11L;

        mockMvc.perform(delete("/users/permanent/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted permanently!"));

        verify(userService, times(1)).deleteUserPermanently(userId);
    }
}
