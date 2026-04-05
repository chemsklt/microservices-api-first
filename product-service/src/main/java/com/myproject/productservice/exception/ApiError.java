package com.myproject.productservice.exception;

import java.time.Instant;

public record ApiError(
        int status,
        String message,
        String path,
        Instant timestamp
) {
}