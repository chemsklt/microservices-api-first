package com.myproject.productservice.repository;

import com.myproject.productservice.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Import(ProductRepositoryCustomImpl.class)
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.7");

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.saveAll(List.of(
                Product.builder().name("iPhone 15").description("Apple").price(BigDecimal.valueOf(999.99)).build(),
                Product.builder().name("iPhone SE").description("Apple").price(BigDecimal.valueOf(499.99)).build(),
                Product.builder().name("Samsung Galaxy S24").description("Samsung").price(BigDecimal.valueOf(899.99)).build()
        ));
    }

    @Test
    void shouldSearchWithFiltersAndPagination() {
        List<Product> allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(3);

        Page<Product> result = productRepository.search(
                "iphone",
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(1000),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "price"))
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("iPhone 15");
    }

    @Test
    void shouldFindAllInsertedProducts() {
        List<Product> allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(3);
    }

    @Test
    void shouldSearchByNameOnly() {
        Page<Product> result = productRepository.search(
                "iphone",
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldSearchByPriceRangeOnly() {
        Page<Product> result = productRepository.search(
                null,
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(1000),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price"))
        );

        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
