package com.bit.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a token entity in the application.
 * This entity is mapped to the "tokens" table in the database.
 *
 * @author Emirhan Tuygun
 */
@Table(name = "tokens")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "is_logged_out")
    private boolean loggedOut;


}
