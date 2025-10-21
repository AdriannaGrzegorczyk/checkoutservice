package com.example.checkoutservice.controller.models;

import java.time.LocalDateTime;

public record AddProductResponse(LocalDateTime timestamp, String productName) {
}
