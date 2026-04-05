package com.myproject.inventoryservice.service;

import com.myproject.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldReturnTrueWhenProductIsInStock() {
        String skuCode = "iphone_13";
        Integer quantity = 2;

        when(inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity))
                .thenReturn(true);

        boolean result = inventoryService.isInStock(skuCode, quantity);

        assertThat(result).isTrue();
        verify(inventoryRepository).existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity);
    }

    @Test
    void shouldReturnFalseWhenProductIsNotInStock() {
        String skuCode = "iphone_13";
        Integer quantity = 5;

        when(inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity))
                .thenReturn(false);

        boolean result = inventoryService.isInStock(skuCode, quantity);

        assertThat(result).isFalse();
        verify(inventoryRepository).existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity);
    }
}