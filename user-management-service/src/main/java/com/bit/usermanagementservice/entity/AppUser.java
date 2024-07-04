package com.bit.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user entity in the application.
 *
 * @author Emirhan Tuygun
 */
@Entity
@Table(name = "users",
        indexes = {@Index(name = "idx_name_username_email_roles", columnList="name, username, email, roles")}
)
@SoftDelete
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private List<Role> roles = new ArrayList<>();
}