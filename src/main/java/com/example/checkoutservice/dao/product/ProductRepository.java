package com.example.checkoutservice.dao.product;

import java.util.List;

public interface ProductRepository {

    Product getByName(String name);
    List<Product> getByIds(List<Long> ids);
}
