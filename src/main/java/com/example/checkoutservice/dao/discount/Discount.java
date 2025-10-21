package com.example.checkoutservice.dao.discount;



import java.util.List;


public record Discount(long discountId, String description, int discountValue, List<DiscountCombo> discountCombos) {


}
