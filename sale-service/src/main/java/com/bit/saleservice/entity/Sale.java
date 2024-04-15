package com.bit.saleservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cashier;
    private Date date;

    @Column(name = "payment_method")
    private Payment paymentMethod;


    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(
            name = "user_campaigns",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "campaign_id"))
    private List<Campaign> campaigns;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<Product> products;

    private BigDecimal total;
}