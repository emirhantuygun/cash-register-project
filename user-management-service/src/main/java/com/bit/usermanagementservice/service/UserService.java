package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * This interface defines the contract for the User Service.
 * It provides methods for managing user data.
 *
 * @author Emirhan Tuygun
 */
public interface UserService {

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The unique identifier of the user.
     * @return The UserResponse object representing the user.
     */
    UserResponse getUser(Long id);

    /**
     * Retrieves all active users.
     *
     * @return A list of UserResponse objects representing all active users.
     */
    List<UserResponse> getAllUsers();

    /**
     * Retrieves all deleted users.
     *
     * @return A list of UserResponse objects representing all deleted users.
     */
    List<UserResponse> getDeletedUsers();

    /**
     * Retrieves a paginated list of users, filtered and sorted based on the provided parameters.
     *
     * @param page The page number to retrieve.
     * @param size The number of users per page.
     * @param sortBy The field to sort by.
     * @param direction The sorting direction (asc or desc).
     * @param name The name filter.
     * @param username The username filter.
     * @param email The email filter.
     * @param roleName The role name filter.
     * @return A Page object containing the list of UserResponse objects.
     */
    Page<UserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String username, String email, String roleName);

    /**
     * Creates a new user based on the provided UserRequest object.
     *
     * @param userRequest The UserRequest object containing the user data.
     * @return The UserResponse object representing the newly created user.
     */
    UserResponse createUser(UserRequest userRequest);

    /**
     * Updates an existing user based on the provided unique identifier and UserRequest object.
     *
     * @param id The unique identifier of the user to update.
     * @param userRequest The UserRequest object containing the updated user data.
     * @return The UserResponse object representing the updated user.
     */
    UserResponse updateUser(Long id, UserRequest userRequest);

    /**
     * Deletes a user based on the provided unique identifier.
     * The user will be marked as deleted, but not permanently removed.
     *
     * @param id The unique identifier of the user to delete.
     */
    void deleteUser(Long id);

    /**
     * Restores a deleted user based on the provided unique identifier.
     * The user will be marked as active again.
     *
     * @param id The unique identifier of the user to restore.
     * @return The UserResponse object representing the restored user.
     */
    UserResponse restoreUser(Long id);

    /**
     * Permanently deletes a user based on the provided unique identifier.
     * The user will be removed from the system.
     *
     * @param id The unique identifier of the user to delete permanently.
     */
    void deleteUserPermanently(Long id);
}
