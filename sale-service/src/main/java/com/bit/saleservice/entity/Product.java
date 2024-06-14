package com.bit.saleservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@SoftDelete
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String name;

    @Column(name = "barcode_number")
    private String barcodeNumber;

    private Integer quantity;
    private BigDecimal price;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sale_id")
    private Sale sale;
}
