package com.myproject.orderservice.dto;

public record InventoryResponse(
        String skuCode,
        Integer quantity,
        Boolean inStock
) {}
