# Discount Rules Engine for Retail Store

##  Overview
The Discount Rules Engine for the retail store is a Scala-based application designed to automate the process of discount calculation. This application reads order data from a CSV file, checks each order against a set of predefined qualifying rules, calculates the applicable discount and total price, and inserts the processed data into a MySQL database.

## Discount Rules and Calculations:

### 1-Less Than 30 Days to Expiry Qualifier Rule:
 Checks if there are less than 30 days remaining for the product to expire.
 
    Discounts:
    Gradual discount based on days remaining, starting from 1% and increasing by 1% per day, up to a maximum of 30%.
    
### 2-Cheese and Wine Qualifier Rule:
 Identifies orders containing wine or cheese products.
 
    Discounts:
    5% discount for wine products, 10% discount for cheese products.

### 3-More Than 5 Qualifier Rule:
 Checks if the quantity of a product in an order exceeds 5 units.
 
    Discounts:
    5% discount for quantities 6-9 units.
    
    7% discount for quantities 10-14 units.
    
    10% discount for quantities more than 15 units.

### 4-Products Sold on 23rd of March Qualifier Rule:
 Identifies orders made on the 23rd of March.
 
    Discount:
    50% discount for orders made on this date.

### 5-App Usage Qualifier Rule:
 Checks if the sale was made through the App.
 
    Discounts:
    5% discount for quantities 1-5 units.
  
    10% discount for quantities 6-10 units.
    
    15% discount for quantities more than 10 units.

### 6-Visa Card Usage Qualifier Rule:
 Identifies orders made using Visa cards.
 
    Discount:
    5% discount.


## Database Interaction
### Database Connection:
   Utilizes MySQL Connector/J Driver: The application utilizes the MySQL Connector/J driver for establishing connectivity with the database.

### Data Insertion:

   Inserts Processed Data: Inserts processed data into the "orders" table in the database.
  
   Details Included: Includes order date, expiry date, product name, quantity, unit price, channel, payment method, discount, and total price in the inserted data

## Logging Mechanisms

### Logging Engine Rule Interactions:

   Function: logMessage
  
   Purpose: Records details of engine rule processing.
  
   Description: Captures information related to engine rule interactions and stores it in the Rules_Engine.log file.
  
   Structure: Each log entry includes a timestamp, log level, and message pertaining to the rule interaction event.

