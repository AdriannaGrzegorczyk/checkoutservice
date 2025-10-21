package com.example.checkoutservice.dao;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String product) {
        super("Product with " + product + " name has not been found!");
    }
}