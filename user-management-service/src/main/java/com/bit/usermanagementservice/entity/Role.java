package com.bit.usermanagementservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Role entity in the application.
 * This entity is mapped to the 'roles' table in the database.
 *
 * @author Emirhan Tuygun
 */
@Table(name = "roles")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String roleName;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles", cascade = CascadeType.DETACH)
    @Builder.Default
    private List<AppUser> users = new ArrayList<>();

    public Role (String roleName){
        this.roleName = roleName;
    }
}