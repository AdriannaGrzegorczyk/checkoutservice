package com.example.checkoutservice.integration;

import com.example.checkoutservice.dao.ProductNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CheckoutServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
    }

    @Test
    void testGetProduct() throws Exception {
        String response = mockMvc.perform(get("/product/A").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode actualResponse = objectMapper.readTree(response);
        String expectedResponse = """
                    {
                      "productName": "A",
                      "price": 4000
                    }
                """;
        Assertions.assertEquals(objectMapper.readTree(expectedResponse).get("productName"), actualResponse.get("productName"));
        Assertions.assertEquals(objectMapper.readTree(expectedResponse).get("price"), actualResponse.get("price"));
    }

    @Test
    void testGetProductThrowsProductNotFoundException() throws Exception {
        mockMvc.perform(get("/product/Z").session(session))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        Assertions.assertInstanceOf(ProductNotFoundException.class, result.getResolvedException())
                );
    }


    @Test
    void testAddProduct() throws Exception {
        String response = mockMvc.perform(post("/product/A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode actualResponse = objectMapper.readTree(response);
        JsonNode expectedResponse = objectMapper.readTree("""
                    {
                      "productName": "A",
                      "timestamp": "2025-10-21T16:26:36.3656004"
                    }
                """);
        Assertions.assertEquals(expectedResponse.get("productName"), actualResponse.get("productName"));
    }

    @Test
    void testAddProductThrowsProductNotFoundException() throws Exception {
        mockMvc.perform(post("/product/Z").session(session))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        Assertions.assertInstanceOf(ProductNotFoundException.class, result.getResolvedException())
                );
    }

    @Test
    void testGetReceipt() throws Exception {
        mockMvc.perform(post("/product/A").contentType(MediaType.APPLICATION_JSON).content("{}").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/product/A").contentType(MediaType.APPLICATION_JSON).content("{}").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/product/A").contentType(MediaType.APPLICATION_JSON).content("{}").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/product/B").contentType(MediaType.APPLICATION_JSON).content("{}").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/product/B").contentType(MediaType.APPLICATION_JSON).content("{}").session(session))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/receipt").session(session))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectNode actualResponse = (ObjectNode) objectMapper.readTree(response);
        actualResponse.remove("timestamp");
        JsonNode expectedResponse = objectMapper.readTree("""
                {
                  "receiptProducts" : [ {
                    "name" : "A",
                    "quantity" : 3,
                    "pricePerUnit" : 4000,
                    "total" : 12000
                  }, {
                    "name" : "B",
                    "quantity" : 2,
                    "pricePerUnit" : 1000,
                    "total" : 2000
                  } ],
                  "receiptDiscounts" : [ {
                    "discountDescription" : "3 x A",
                    "quantity" : 1,
                    "discountUnit" : -3000,
                    "totalDiscount" : -3000
                  }, {
                    "discountDescription" : "A + B",
                    "quantity" : 2,
                    "discountUnit" : -500,
                    "totalDiscount" : -1000
                  }, {
                    "discountDescription" : "2 x B",
                    "quantity" : 1,
                    "discountUnit" : -500,
                    "totalDiscount" : -500
                  } ],
                  "total" : 9500
                }
                """);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }
}
