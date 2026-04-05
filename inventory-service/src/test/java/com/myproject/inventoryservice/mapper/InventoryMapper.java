package com.myproject.inventoryservice.mapper;

import com.myproject.inventoryservice.generated.model.InventoryResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryMapperTest {

    private final InventoryMapper inventoryMapper = new InventoryMapper();

    @Test
    void shouldMapToInventoryResponse() {
        InventoryResponse response = inventoryMapper.toResponse("iphone_13", 2, true);

        assertThat(response).isNotNull();
        assertThat(response.getSkuCode()).isEqualTo("iphone_13");
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getInStock()).isTrue();
    }
}