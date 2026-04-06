package com.myproject.productservice.controller;

import com.myproject.productservice.generated.api.ProductsApi;
import com.myproject.productservice.generated.model.ProductPageResponse;
import com.myproject.productservice.generated.model.ProductRequest;
import com.myproject.productservice.generated.model.ProductResponse;
import com.myproject.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController implements ProductsApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest) {
        log.info("Received request to create product with name={}", productRequest.getName());
        ProductResponse createdProduct = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Override
    public ResponseEntity<ProductPageResponse> getAllProducts(String name, Double minPrice, Double maxPrice, Integer page, Integer size, String sort) {
//        try {
//            Thread.sleep(5000);
//        }catch (InterruptedException e){
//            throw new RuntimeException(e);
//        }
        log.info(
                "Received request to get products with filters name={}, minPrice={}, maxPrice={}, page={}, size={}, sort={}",
                name, minPrice, maxPrice, page, size, sort
        );

        return ResponseEntity.ok(
                productService.getAllProducts(name, minPrice, maxPrice, page, size, sort)
        );
    }
}