package com.myproject.productservice.service;


import com.myproject.productservice.domain.Product;
import com.myproject.productservice.generated.model.ProductPageResponse;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.generated.model.ProductResponse;
import com.myproject.productservice.mapper.ProductMapper;
import com.myproject.productservice.repository.ProductRepository;
import com.myproject.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest()
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        Product product = Product.builder()
                .name("iPhone 15")
                .description("Apple phone")
                .price(BigDecimal.valueOf(999.99))
                .build();

        Product savedProduct = Product.builder()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(BigDecimal.valueOf(999.99))
                .build();

        ProductResponse response = new ProductResponse()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo("product-1");
        assertThat(result.getName()).isEqualTo("iPhone 15");
        verify(productRepository).save(product);
    }

    @Test
    void shouldReturnPagedProducts() {
        Product product = Product.builder()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(BigDecimal.valueOf(999.99))
                .build();

        ProductResponse productResponse = new ProductResponse()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        Page<Product> page = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price")),
                1
        );

        when(productRepository.search(eq("iphone"), eq(BigDecimal.valueOf(500.0)), eq(BigDecimal.valueOf(1500.0)), any(Pageable.class)))
                .thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductPageResponse result = productService.getAllProducts(
                "iphone", 500.0, 1500.0, 0, 10, "price,desc"
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenMinPriceGreaterThanMaxPrice() {
        assertThatThrownBy(() ->
                productService.getAllProducts("iphone", 1500.0, 500.0, 0, 10, "price,desc")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minPrice must be less than or equal to maxPrice");
    }

    @Test
    void shouldUseDefaultPaginationAndSortWhenParametersAreNull() {
        Page<Product> emptyPage = Page.empty(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")));

        when(productRepository.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.getAllProducts(null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(isNull(), isNull(), isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void shouldUseProvidedSortDirection() {
        Page<Product> emptyPage = Page.empty(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "price")));

        when(productRepository.search(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.getAllProducts(null, null, null, 0, 5, "price,desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(isNull(), isNull(), isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "price"));
    }
}
