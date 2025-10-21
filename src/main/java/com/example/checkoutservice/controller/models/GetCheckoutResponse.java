package com.example.checkoutservice.controller.models;

import java.time.LocalDateTime;
import java.util.List;

public record GetCheckoutResponse(LocalDateTime timestamp, List<CheckoutProductRecord> receiptProducts, List<CheckoutDiscountRecord> receiptDiscounts, Integer total) {
}
