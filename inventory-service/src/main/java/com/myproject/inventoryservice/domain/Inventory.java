package com.myproject.inventoryservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_sku_code", columnNames = "sku_code"),
        indexes = @Index(name = "idx_inventory_sku_code", columnList = "sku_code")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false, unique = true, length = 100)
    private String skuCode;

    @Column(nullable = false)
    private Integer quantity;
}