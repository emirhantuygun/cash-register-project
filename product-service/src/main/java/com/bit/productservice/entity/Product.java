package com.bit.productservice.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import java.math.BigDecimal;

/**
 * Represents a product entity in the system.
 *
 * @author Emirhan Tuygun
 */
@Entity
@Table(name = "products",
        indexes = {@Index(name = "idx_name_stockQuantity_price", columnList="name, stock_quantity, price")}
)
@SoftDelete
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Column(name = "barcode_number")
    private String barcodeNumber;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    private BigDecimal price;
}