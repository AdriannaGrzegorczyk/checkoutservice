package com.example.checkoutservice.service;

import com.example.checkoutservice.controller.models.*;
import com.example.checkoutservice.dao.discount.Discount;
import com.example.checkoutservice.dao.discount.DiscountCombo;
import com.example.checkoutservice.dao.discount.DiscountRepository;
import com.example.checkoutservice.dao.product.Product;
import com.example.checkoutservice.dao.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CheckoutService {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final Clock clock;

    CheckoutService(ProductRepository productRepository, DiscountRepository discountRepository, Clock clock) {
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
        this.clock = clock;
    }

    public GetProductResponse getProduct(String name) {
        Product product = productRepository.getByName(name);
        return new GetProductResponse(LocalDateTime.now(clock), product.name(), product.price());
    }

    public AddProductResponse addProduct(String name, List<Long> products) {
        Long newProductId = productRepository.getByName(name).productId();
        products.add(newProductId);
        return new AddProductResponse(LocalDateTime.now(clock), name);
    }

    public GetCheckoutResponse getReceipt(List<Long> productIds) {

        Set<Long> uniqueIds = new HashSet<>(productIds);
        Map<Long, Product> idToProduct = new HashMap<>();
        List<Product> products = productRepository.getByIds(productIds);
        products.forEach(product -> idToProduct.putIfAbsent(product.productId(), product));
        Map<Long, Integer> counted = countProducts(products);
        List<CheckoutProductRecord> receiptProducts = counted.keySet().stream()
                .map(productId -> mapProductToCheckoutRecord(idToProduct.get(productId), counted.get(productId)))
                .toList();
        List<Discount> discounts = discountRepository.getDiscountsByProductIds(uniqueIds);
        List<CheckoutDiscountRecord> receiptDiscounts = processDiscounts(counted, discounts);
        int receiptTotal = receiptProducts.stream().mapToInt(CheckoutProductRecord::total)
                .sum() + receiptDiscounts.stream().mapToInt(CheckoutDiscountRecord::totalDiscount).sum();
        return new GetCheckoutResponse(LocalDateTime.now(clock), receiptProducts, receiptDiscounts, receiptTotal);
    }


    private List<CheckoutDiscountRecord> processDiscounts(Map<Long, Integer> countedProducts, List<Discount> discounts) {
        List<CheckoutDiscountRecord> discountRecords = new ArrayList<>();
        for (Discount discount : discounts) {
            int maxDiscountMultiplier = 0;
            for (DiscountCombo discountCombo : discount.discountCombos()) {
                if (countedProducts.containsKey(discountCombo.productId()) && countedProducts.get(discountCombo.productId()) >= discountCombo.requiredQuantity()) {
                    int discountMultiplier = countedProducts.get(discountCombo.productId()) / discountCombo.requiredQuantity();
                    maxDiscountMultiplier = maxDiscountMultiplier == 0 ? discountMultiplier : Math.min(maxDiscountMultiplier, discountMultiplier);
                } else {
                    maxDiscountMultiplier = 0;
                    break;
                }
            }
            if (maxDiscountMultiplier > 0) {
                discountRecords.add(new CheckoutDiscountRecord(discount.description(), maxDiscountMultiplier, discount.discountValue(), discount.discountValue() * maxDiscountMultiplier));
            }
        }
        return discountRecords.stream().sorted(Comparator.comparing(CheckoutDiscountRecord::totalDiscount)).toList();
    }

    private CheckoutProductRecord mapProductToCheckoutRecord(Product product, int quantity) {
        int totalPrice = product.price() * quantity;
        return new CheckoutProductRecord(product.name(), quantity, product.price(), totalPrice);
    }

    private Map<Long, Integer> countProducts(List<Product> products) {
        Map<Long, Integer> map = new HashMap<>();
        for (Product product : products) {
            map.put(product.productId(), map.getOrDefault(product.productId(), 0) + 1);
        }
        return map;
    }
}
