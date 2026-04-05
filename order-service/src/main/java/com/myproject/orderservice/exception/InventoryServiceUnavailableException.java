package com.myproject.orderservice.exception;

public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryServiceUnavailableException(String message) {
        super(message);
    }
}
