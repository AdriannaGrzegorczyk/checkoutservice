package com.example.checkoutservice.controller;

import com.example.checkoutservice.controller.models.AddProductResponse;
import com.example.checkoutservice.controller.models.GetCheckoutResponse;
import com.example.checkoutservice.controller.models.GetProductResponse;
import com.example.checkoutservice.service.CheckoutService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }
    @GetMapping("/product/{productName}")
    public GetProductResponse getProduct(@PathVariable String productName){
        return checkoutService.getProduct(productName);
    }

    @PostMapping("/product/{productName}")
    public AddProductResponse scanItem(@PathVariable String productName, HttpSession httpSession) {
        List<Long> products = (List<Long>) Optional.ofNullable(httpSession.getAttribute("products"))
                .orElse(new ArrayList<>());
        AddProductResponse response = checkoutService.addProduct(productName, products);
        httpSession.setAttribute("products", products);
        return response;
    }

    @GetMapping("/receipt")
    public GetCheckoutResponse getReceipt(HttpSession httpSession) {
        List<Long> products = (List<Long>) Optional.ofNullable(httpSession.getAttribute("products"))
                .orElse(new ArrayList<>());
        return checkoutService.getReceipt(products);
    }
}
