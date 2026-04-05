package com.myproject.productservice.mapper;

import com.myproject.productservice.domain.Product;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.generated.model.ProductResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName() != null ? request.getName().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : null)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductResponse()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice() != null ? product.getPrice().doubleValue() : null);
    }
}