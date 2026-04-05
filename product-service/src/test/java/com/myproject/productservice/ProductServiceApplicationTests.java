package com.myproject.productservice;

import com.myproject.productservice.domain.Product;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.repository.ProductRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductServiceApplicationTests {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.7");

    @LocalServerPort
    private Integer port;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() {
        ProductRequest productRequest = getProductRequest();

        RestAssured.given()
                .contentType("application/json")
                .body(productRequest)
                .when()
                .post("/api/products")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo(productRequest.getName()))
                .body("description", Matchers.equalTo(productRequest.getDescription()))
                .body("price", Matchers.equalTo(productRequest.getPrice().floatValue()));
    }

    @Test
    void shouldGetAllProductsWithPagination() {
        productRepository.saveAll(List.of(
                Product.builder().name("iPhone 15").description("Apple").price(BigDecimal.valueOf(999.99)).build(),
                Product.builder().name("Samsung Galaxy S24").description("Samsung").price(BigDecimal.valueOf(899.99)).build(),
                Product.builder().name("Pixel 8").description("Google").price(BigDecimal.valueOf(799.99)).build()
        ));

        RestAssured.given()
                .queryParam("page", 0)
                .queryParam("size", 2)
                .queryParam("sort", "name,asc")
                .when()
                .get("/api/products")
                .then()
                .log().all()
                .statusCode(200)
                .body("content.size()", Matchers.equalTo(2))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(2))
                .body("totalElements", Matchers.equalTo(3))
                .body("totalPages", Matchers.equalTo(2));
    }

    @Test
    void shouldFilterProductsByNameAndPriceRange() {
        productRepository.saveAll(List.of(
                Product.builder().name("iPhone 15").description("Apple").price(BigDecimal.valueOf(999.99)).build(),
                Product.builder().name("iPhone SE").description("Apple").price(BigDecimal.valueOf(499.99)).build(),
                Product.builder().name("Samsung Galaxy S24").description("Samsung").price(BigDecimal.valueOf(899.99)).build()
        ));

        RestAssured.given()
                .queryParam("name", "iphone")
                .queryParam("minPrice", 500)
                .queryParam("maxPrice", 1200)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "price,desc")
                .when()
                .get("/api/products")
                .then()
                .log().all()
                .statusCode(200)
                .body("content.size()", Matchers.equalTo(1))
                .body("content[0].name", Matchers.equalTo("iPhone 15"))
                .body("totalElements", Matchers.equalTo(1));
    }

    @Test
    void shouldReturnBadRequestWhenMinPriceGreaterThanMaxPrice() {
        RestAssured.given()
                .queryParam("minPrice", 1500)
                .queryParam("maxPrice", 500)
                .when()
                .get("/api/products")
                .then()
                .log().all()
                .statusCode(400);
    }

    private ProductRequest getProductRequest() {
        return new ProductRequest()
                .name("iPhone 13")
                .description("iPhone 13")
                .price(BigDecimal.valueOf(1200).doubleValue());
    }
}