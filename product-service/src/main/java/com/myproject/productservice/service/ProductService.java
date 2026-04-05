package com.myproject.productservice.service;

import com.myproject.productservice.domain.Product;
import com.myproject.productservice.generated.model.ProductPageResponse;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.generated.model.ProductResponse;
import com.myproject.productservice.mapper.ProductMapper;
import com.myproject.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        Product savedProduct = productRepository.save(product);

        log.info("Product created with id={}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    public ProductPageResponse getAllProducts(String name, Double minPrice, Double maxPrice, Integer page, Integer size, String sort) {

        validatePriceRange(minPrice, maxPrice);

        Pageable pageable = buildPageable(page, size, sort);

        Page<Product> productPage = productRepository.search(
                name,
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                pageable
        );

        log.info(
                "Retrieved {} products out of total {} for filters name={}, minPrice={}, maxPrice={}, page={}, size={}, sort={}",
                productPage.getNumberOfElements(), productPage.getTotalElements(), name,
                minPrice, maxPrice, page, size, sort);

        return new ProductPageResponse()
                .content(productPage.getContent().stream().map(productMapper::toResponse).toList())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements((long) productPage.getTotalElements())
                .totalPages(productPage.getTotalPages());
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }
    }

    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;

        Sort resolvedSort = buildSort(sort);

        return PageRequest.of(resolvedPage, resolvedSize, resolvedSort);
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        String[] sortParts = sort.split(",");

        String field = sortParts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortParts.length > 1) {
            direction = Sort.Direction.fromOptionalString(sortParts[1].trim().toUpperCase())
                    .orElse(Sort.Direction.ASC);
        }

        return Sort.by(direction, field);
    }
}