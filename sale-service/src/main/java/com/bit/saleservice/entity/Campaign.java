package com.bit.saleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "campaigns")
@Entity
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

    @ManyToMany(mappedBy = "campaigns", cascade = CascadeType.DETACH)
    private List<Sale> sales = new ArrayList<>();

}
