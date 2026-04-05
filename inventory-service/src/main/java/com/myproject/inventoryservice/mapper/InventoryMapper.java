package com.myproject.inventoryservice.mapper;

import com.myproject.inventoryservice.generated.model.InventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(String skuCode, Integer quantity, boolean inStock) {
        return new InventoryResponse()
                .skuCode(skuCode)
                .quantity(quantity)
                .inStock(inStock);
    }
}