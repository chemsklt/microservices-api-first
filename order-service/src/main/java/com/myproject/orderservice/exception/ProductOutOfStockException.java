package com.myproject.orderservice.exception;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String skuCode) {
        super("Product with skuCode '%s' is not in stock".formatted(skuCode));
    }
}
