package com.myproject.inventoryservice.service;

import com.myproject.inventoryservice.repository.InventoryRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Observed(name = "inventory.check.stock", contextualName = "check-stock")
    public boolean isInStock(String skuCode, Integer quantity) {
        log.info("Checking stock for skuCode={} and quantity={}", skuCode, quantity);

        boolean inStock = inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity);

        log.info("Stock check result for skuCode={} quantity={} => inStock={}", skuCode, quantity, inStock);
        return inStock;
    }
}