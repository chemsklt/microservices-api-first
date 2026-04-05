package com.myproject.productservice.repository;


import com.myproject.productservice.domain.Product;
import lombok.RequiredArgsConstructor;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Product> search(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            criteria.add(Criteria.where("name")
                    .regex(".*" + Pattern.quote(name) + ".*", "i"));
        }

        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");

            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(new Decimal128(minPrice));
            }

            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(new Decimal128(maxPrice));
            }

            criteria.add(priceCriteria);
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Product.class);

        query.with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class);

        return new PageImpl<>(products, pageable, total);
    }
}