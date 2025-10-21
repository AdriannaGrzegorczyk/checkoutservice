package com.example.checkoutservice.controller.models;

public record CheckoutDiscountRecord(String discountDescription, int quantity, int discountUnit, int totalDiscount) {
}
