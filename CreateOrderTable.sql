-- Select the database to use
USE scala;

-- Create the Orders table with specified columns 
CREATE TABLE Orders (
    OrderDate DATE,
    ProductName VARCHAR(100),
    ExpiryDate DATE,
    Quantity INT,
    UnitPrice DECIMAL(10, 2),
    Channel VARCHAR(50),
    PaymentMethod VARCHAR(50),
    Discount DECIMAL(10, 2),
    TotalPrice DECIMAL(10, 2)
);

-- Count the number of orders with a discount greater than 0
SELECT count(*) as OrderWithDiscount 
FROM scala.Orders
WHERE Discount > 0.0;

-- Count the number of orders with no discount (discount equals 0)
SELECT count(*) as OrderWithoutDiscount 
FROM scala.Orders
WHERE Discount = 0.0;