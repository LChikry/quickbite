
INSERT INTO Users (user_first_name, user_last_name, user_middle_names, user_type)
VALUES
('John', 'Doe', 'Michael', 'Customer'),
('Jane', 'Smith', NULL, 'Employee'),
('Carlos', 'Gomez', 'Luis Manuel', 'Customer'),
('Younes', 'Rahati', NULL, 'Customer'),
('Lahoucine', 'Chikry', NULL, 'Employee'),
('Mohammed', 'Rourou', 'Amine', 'Employee');

INSERT INTO Restaurant (restaurant_name, restaurant_opening_time, restaurant_closing_time)
VALUES
('La Cuisine', '08:00:00', '22:00:00'),
('El Asador', '10:00:00', '23:00:00'),
('Café Bonheur', '07:00:00', '20:00:00');

INSERT INTO Customer (user_id, user_first_name, user_last_name, user_middle_names, customer_balance, currency, user_type)
VALUES
(1, 'John', 'Doe', 'Michael', 500.00, 'MAD', 'Customer'),
(3, 'Carlos', 'Gomez', 'Luis Manuel', 1500.00, 'MAD', 'Customer'),
(4, 'Younes', 'Rahati', NULL, 1700.00, 'MAD', 'Customer');

INSERT INTO Employee (user_id, user_first_name, user_last_name, user_middle_names, restaurant_id, user_type)
VALUES
(2, 'Jane', 'Smith', NULL, 1, 'Employee'),
(5, 'Lahoucine', 'Chikry', NULL, 2, 'Employee'),
(6, 'Mohammed', 'Rourou', 'Amine', 3, 'Employee');

INSERT INTO Account (account_email, account_password, user_id, account_signup_date)
VALUES
('john.doe@example.com', 'hashedpassword1', 1, '2024-12-01 10:00:00+00'),
('jane.smith@example.com', 'hashedpassword2', 2, '2024-12-02 11:30:00+00'),
('carlos.gomez@example.com', 'hashedpassword3', 3, '2024-12-03 12:00:00+00'),
('younes.rahati@lilo.org', 'hashedpassword4', 4, '2024-12-04 9:30:00+00'),
('lahoucine@example.com', 'hashedpassword5', 5, '2024-12-5 8:00:00+00'),
('rourou@example.com', 'hashedpassword6', 6, '2024-12-6 7:00:00+00');

INSERT INTO Category (category_name)
VALUES
('Appetizers'),
('Main Dishes'),
('Desserts');

-- Products for Restaurant 1
INSERT INTO Product (restaurant_id, category_id, product_name, product_price, available_quantity, min_quantity_allowed)
VALUES
(1, 1, 'Spring Rolls', 30.00, 100, 10),       -- Appetizers
(1, 2, 'Grilled Salmon', 150.00, 50, 5),      -- Main Dishes
(1, 3, 'Cheesecake', 50.00, 20, 2);           -- Desserts

-- Products for Restaurant 2
INSERT INTO Product (restaurant_id, category_id, product_name, product_price, available_quantity, min_quantity_allowed)
VALUES
(2, 1, 'Garlic Bread', 20.00, 80, 5),         -- Appetizers
(2, 2, 'Beef Steak', 200.00, 40, 5),          -- Main Dishes
(2, 3, 'Apple Pie', 60.00, 25, 2);            -- Desserts

-- Products for Restaurant 3
INSERT INTO Product (restaurant_id, category_id, product_name, product_price, available_quantity, min_quantity_allowed)
VALUES
(3, 1, 'Bruschetta', 25.00, 70, 5),           -- Appetizers
(3, 2, 'Pasta Carbonara', 120.00, 30, 5),     -- Main Dishes
(3, 3, 'Tiramisu', 55.00, 15, 2);             -- Desserts

INSERT INTO Orders (order_type, currency, order_status, restaurant_id, employee_id, customer_id)
VALUES
('Pick-up', 'MAD', 'Pending', 1, 2, 1),
('On-site', 'MAD', 'In Preparation', 2, 5, 3),
('Pick-up', 'MAD', 'Ready', 3, 6, 4);

INSERT INTO Order_Item (order_id, product_id, quantity_ordered, product_unit_price, currency)
VALUES
(1, 1, 2, 30.00, 'MAD'),
(2, 5, 1, 200.00,'MAD'),
(2, 6, 1, 60.00, 'MAD'),
(3, 7, 3, 25.00, 'MAD'),
(3, 8, 2, 120.00, 'MAD'),
(3, 9, 1, 55.00, 'MAD');