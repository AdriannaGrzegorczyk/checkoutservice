package com.example.checkoutservice.dao.product;

public record Product(
        long productId,
        String name,
        int price
) {
}
