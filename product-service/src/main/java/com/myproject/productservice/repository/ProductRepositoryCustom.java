package com.myproject.productservice.repository;

import com.myproject.productservice.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepositoryCustom {
    Page<Product> search(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
