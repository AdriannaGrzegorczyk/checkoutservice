#  Checkout Component 3.0

This project is a simple **Spring-based REST API** that implements a market-style checkout system. It supports scanning products, calculating total prices (with special and bundled pricing rules), and generating receipts.

---

##  Features

-  REST API for scanning and retrieving products
-  Stateful checkout (remembers scanned items)
-  Support for multi-price discounts (e.g., 3 for 30)
-  Support for bundled pricing (e.g., buy X and Y, save Z)
-  Receipt generation with itemized prices
-  Proper exception handling for missing products

---

##  Endpoints

### 1. `GET /product/{productName}`
Returns product details by name.
Example output:
```json 
{
  "productName": "B",
  "price": 1000
}
```


### 2. `POST /product/{productName}`
Scans a product and adds it to the current checkout session.
Example output:
```json 
{
  "productName": "B",
  "price": 1000
}
```

### 3. `GET /receipt`
Returns a full receipt with all scanned products and the total price.
```json
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
```
---

## Details


- This project used in memory hard coded data example stored in _com/example/checkoutservice/dao/product/ProductLocalRepository.java_ which can be easly overridden by new implementation of ProductRepository which could use other persistent methods like DB
- All price are stored in cents to eliminate problem with decimal point values. This solution as well is easy extendable with BigInteger if requirement will be to handle big numbers.



- Example discounts contains **Buy multiple** like this in table below:

  | Item | Normal Price | Bulk Quantity | Bulk Price |
    |------|--------------|----------------|------------|
  | A    | 4000         | 3              | 300        |
  | B    | 1000         | 2              | 750        |
  | C    | 3000         | 4              | 200        |
  | D    | 2500         | 2              | 2350       |

- And **Buy X with Y** discount like:

  | First Item | Second Item | First Quantity | Second Quantity | Discount Quantity |
  |------------|-------------|----------------|-----------------|------------------|
  | A          | B           | 1              | 1               | -500             |


- After scanning, calling `/receipt` generates a full breakdown of items and pricing.

---
