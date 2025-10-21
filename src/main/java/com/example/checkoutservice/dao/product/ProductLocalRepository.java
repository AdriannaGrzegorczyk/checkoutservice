package com.example.checkoutservice.dao.product;

import com.example.checkoutservice.dao.ProductNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProductLocalRepository implements ProductRepository {
    Product A = new Product(1L, "A", 4000);
    Product B = new Product(2L, "B", 1000);
    Product C = new Product(3L, "C", 3000);
    Product D = new Product(4L, "D", 2500);
    Map<Long, Product> store = Map.of(
            1L, A,
            2L, B,
            3L, C,
            4L, D
    );

    @Override
    public Product getByName(String name) {
        return store.values().stream()
                .filter(product -> name.equals(product.name())).findAny()
                .orElseThrow(() -> new ProductNotFoundException(name));
    }

    @Override
    public List<Product> getByIds(List<Long> ids) {
        return ids.stream().map(id -> store.get(id)).toList();
    }
}
