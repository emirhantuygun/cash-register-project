package com.bit.saleservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "campaigns")
@Entity
@SoftDelete
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String details;
    private Date expiration;
    private Boolean isExpired;

    @Builder.Default
    @ManyToMany(mappedBy = "campaigns", cascade = CascadeType.DETACH)
    private List<Sale> sales = new ArrayList<>();
}
