package com.myproject.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.productservice.generated.model.ProductPageResponse;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.generated.model.ProductResponse;
import com.myproject.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest()
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        ProductResponse response = new ProductResponse()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        when(productService.createProduct(request)).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.description").value("Apple phone"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse()
                .id("product-1")
                .name("iPhone 15")
                .description("Apple phone")
                .price(999.99);

        ProductPageResponse pageResponse = new ProductPageResponse()
                .content(List.of(productResponse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1);

        when(productService.getAllProducts(
                eq("iphone"),
                eq(500.0),
                eq(1500.0),
                eq(0),
                eq(10),
                eq("price,desc")
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("name", "iphone")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("product-1"))
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
}
