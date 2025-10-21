package com.example.checkoutservice.service;


import com.example.checkoutservice.controller.models.GetCheckoutResponse;
import com.example.checkoutservice.controller.models.GetProductResponse;
import com.example.checkoutservice.dao.ProductNotFoundException;
import com.example.checkoutservice.dao.discount.Discount;
import com.example.checkoutservice.dao.discount.DiscountCombo;
import com.example.checkoutservice.dao.discount.DiscountRepository;
import com.example.checkoutservice.dao.product.Product;
import com.example.checkoutservice.dao.product.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.*;

public class CheckoutServiceTest {

    ProductRepository productRepository = Mockito.mock(ProductRepository.class);
    DiscountRepository discountRepository = Mockito.mock(DiscountRepository.class);
    Clock clock = Clock.fixed(LocalDateTime.of(2000,10, 10, 10, 10).toInstant(ZoneOffset.UTC),ZoneId.of("UTC"));
    CheckoutService checkoutService = new CheckoutService(productRepository, discountRepository, clock);
    static ObjectMapper objectMapper = new ObjectMapper();

    private static final Product A = new Product(1L, "A", 3000);
    private static final Product B = new Product(2L, "B", 4000);
    private static final DiscountCombo A_WITH_B_COMBO_A = new DiscountCombo(1L, 1L, 1);
    private static final DiscountCombo A_WITH_B_COMBO_B = new DiscountCombo(1L, 2L, 1);
    private static final DiscountCombo THREE_TIMES_A_COMBO = new DiscountCombo(1L, 1L, 3);
    private static final Discount A_WITH_B_DISCOUNT = new Discount(1L, "A + B", -1000, List.of(A_WITH_B_COMBO_A, A_WITH_B_COMBO_B));
    private static final Discount THREE_TIMES_A_DISCOUNT = new Discount(1L, "3 x A", -1000, List.of(THREE_TIMES_A_COMBO));

    @BeforeAll
    public static void setup(){
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void addProductTest() {
        List<Long> ids = new ArrayList<>(List.of(1L, 2L, 3L));
        when(productRepository.getByName("A")).thenReturn(new Product(1L, "A", 0));
        checkoutService.addProduct("A", ids);
        verify(productRepository, times(1)).getByName("A");
        Assertions.assertEquals(4, ids.size());
        Assertions.assertEquals(1L, ids.get(3));
    }

    @Test
    public void addProductTestThrowsException() {
        when(productRepository.getByName("A")).thenThrow(new ProductNotFoundException("A"));
        Assertions.assertThrows(ProductNotFoundException.class, ()->checkoutService.addProduct("A", List.of()));
    }


    @Test
    public void getProductTest() {
        when(productRepository.getByName("A")).thenReturn(new Product(1L, "A", 10));
        GetProductResponse response = checkoutService.getProduct("A");
        Assertions.assertEquals(10, response.price());
        Assertions.assertEquals("A", response.productName());
    }

    @Test
    public void getProductTestThrowsException() {
        when(productRepository.getByName("A")).thenThrow(new ProductNotFoundException("A"));
        Assertions.assertThrows(ProductNotFoundException.class, ()->checkoutService.getProduct("A"));
    }

    @Test
    public void getReceiptTest() throws JsonProcessingException, JSONException {
        List<Long> ids = new ArrayList<>(List.of(1L, 2L, 1L, 1L, 2L));
        when(discountRepository.getDiscountsByProductIds(new HashSet<>(ids))).thenReturn(List.of(THREE_TIMES_A_DISCOUNT, A_WITH_B_DISCOUNT));
        when(productRepository.getByName("A")).thenReturn(new Product(1L, "A", 10));
        when(productRepository.getByName("B")).thenReturn(new Product(2L, "B", 10));
        when(productRepository.getByIds(ids)).thenReturn(List.of(A, B, A, A, B));
        GetCheckoutResponse response = checkoutService.getReceipt(ids);
        JSONAssert.assertEquals(GET_RECEIPT_EXPECTED_RESPONSE, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response), true);
    }


    private final String GET_RECEIPT_EXPECTED_RESPONSE = """
           {
               "timestamp" : "2000-10-10T10:10:00",
               "receiptProducts" : [ {
                 "name" : "A",
                 "quantity" : 3,
                 "pricePerUnit" : 3000,
                 "total" : 9000
               }, {
                 "name" : "B",
                 "quantity" : 2,
                 "pricePerUnit" : 4000,
                 "total" : 8000
               } ],
               "receiptDiscounts" : [
                {
                 "discountDescription" : "A + B",
                 "quantity" : 2,
                 "discountUnit" : -1000,
                 "totalDiscount" : -2000
               },
               {
                 "discountDescription" : "3 x A",
                 "quantity" : 1,
                 "discountUnit" : -1000,
                 "totalDiscount" : -1000
               }],
               "total" : 14000
             }
    """;
}
