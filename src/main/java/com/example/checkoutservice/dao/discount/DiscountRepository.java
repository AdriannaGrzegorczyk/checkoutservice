package com.example.checkoutservice.dao.discount;

import java.util.List;
import java.util.Set;

public interface DiscountRepository {
    List<Discount> getDiscountsByProductIds(Set<Long> productIds);
}
