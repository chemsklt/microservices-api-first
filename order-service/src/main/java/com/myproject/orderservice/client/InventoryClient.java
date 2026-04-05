package com.myproject.orderservice.client;

import com.myproject.orderservice.dto.InventoryResponse;
import com.myproject.orderservice.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetExchange("/api/inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    InventoryResponse  isInStock(@RequestParam("skuCode") String skuCode, @RequestParam("quantity") Integer quantity);

    default InventoryResponse fallbackMethod(String skuCode, Integer quantity, Throwable throwable) {
        log.warn("Inventory fallback triggered for skuCode={}, quantity={}, reason={}",
                skuCode, quantity, throwable.getMessage());

        throw new InventoryServiceUnavailableException(
                "Inventory service is unavailable for skuCode '%s'".formatted(skuCode),
                throwable
        );
    }
}
