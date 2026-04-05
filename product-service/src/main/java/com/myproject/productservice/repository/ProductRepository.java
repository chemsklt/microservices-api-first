package com.myproject.productservice.repository;

import com.myproject.productservice.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String>, ProductRepositoryCustom {

}
