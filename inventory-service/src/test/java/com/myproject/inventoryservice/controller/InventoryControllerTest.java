package com.myproject.inventoryservice.controller;

import com.myproject.inventoryservice.mapper.InventoryMapper;
import com.myproject.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(InventoryMapper.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void shouldReturnInStockTrue() throws Exception {
        when(inventoryService.isInStock("iphone_13", 2)).thenReturn(true);

        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_13")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode").value("iphone_13"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void shouldReturnInStockFalse() throws Exception {
        when(inventoryService.isInStock("iphone_13", 10)).thenReturn(false);

        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_13")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode").value("iphone_13"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.inStock").value(false));
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsLessThanOne() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_13")
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSkuCodeIsMissing() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("quantity", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsMissing() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_13"))
                .andExpect(status().isBadRequest());
    }
}