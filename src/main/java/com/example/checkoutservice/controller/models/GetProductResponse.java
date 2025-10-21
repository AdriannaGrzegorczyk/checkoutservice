package com.example.checkoutservice.controller.models;


import java.time.LocalDateTime;

public record GetProductResponse(LocalDateTime timestmap, String productName, Integer price) {
}
