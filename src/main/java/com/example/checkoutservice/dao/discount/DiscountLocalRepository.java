package com.example.checkoutservice.dao.discount;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class DiscountLocalRepository implements DiscountRepository {

    DiscountCombo combo1A = new DiscountCombo(1L, 1L, 1);
    DiscountCombo combo1B = new DiscountCombo(1L, 2L, 1);
    DiscountCombo comboMultipleA = new DiscountCombo(2L, 1L, 3);
    DiscountCombo comboMultipleB = new DiscountCombo(3L, 2L, 2);
    DiscountCombo comboMultipleC = new DiscountCombo(4L, 3L, 4);
    DiscountCombo comboMultipleD = new DiscountCombo(5L, 4L, 2);
    Map<Long, Discount> store = Map.of(
            1L, new Discount(1L, "A + B", -500, List.of(combo1A, combo1B)),
            2L, new Discount(2L, "3 x A", -3000, List.of(comboMultipleA)),
            3L, new Discount(3L, "2 x B", -500, List.of(comboMultipleB)),
            4L, new Discount(4L, "4 x C", -4000, List.of(comboMultipleC)),
            5L, new Discount(5L, "2 x D", -500, List.of(comboMultipleD))
    );

    public List<Discount> getDiscountsByProductIds(Set<Long> productIds) {
        return store.values().stream().filter(discount -> discount.discountCombos().stream()
                .anyMatch(combo -> productIds.contains(combo.productId()))).toList();
    }

}
