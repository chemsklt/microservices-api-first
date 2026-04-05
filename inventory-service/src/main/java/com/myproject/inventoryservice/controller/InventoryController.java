package com.myproject.inventoryservice.controller;

import com.myproject.inventoryservice.generated.api.InventoryApi;
import com.myproject.inventoryservice.generated.model.InventoryResponse;
import com.myproject.inventoryservice.mapper.InventoryMapper;
import com.myproject.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InventoryController implements InventoryApi {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @Override
    public ResponseEntity<InventoryResponse> isInStock(String skuCode, Integer quantity) {
        boolean inStock = inventoryService.isInStock(skuCode, quantity);
        return ResponseEntity.ok(inventoryMapper.toResponse(skuCode, quantity, inStock));
    }
}