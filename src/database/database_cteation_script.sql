------------------------- Drop the tables if they exist
DROP TABLE IF EXISTS Account CASCADE;
DROP TABLE IF EXISTS Order_Item CASCADE;
DROP TABLE IF EXISTS Orders CASCADE;
DROP TABLE IF EXISTS Product CASCADE;
DROP TABLE IF EXISTS Category CASCADE;
DROP TABLE IF EXISTS Employee CASCADE;
DROP TABLE IF EXISTS Restaurant CASCADE;
DROP TABLE IF EXISTS Customer CASCADE;
DROP TABLE IF EXISTS Users CASCADE;



--------------------------- Create Tables
CREATE TABLE Users (
	user_id                 SERIAL PRIMARY KEY,
	user_first_name         VARCHAR(15) NOT NULL,
	user_last_name          VARCHAR(25) NOT NULL,
	user_middle_names       VARCHAR(40),
	user_type               VARCHAR(10) NOT NULL

	CONSTRAINT ck_user_type CHECK (user_type IN ('Customer', 'Employee'))
);

CREATE TABLE Account (
	account_id              SERIAL PRIMARY KEY,
	account_email           VARCHAR(100) NOT NULL,
	account_password        VARCHAR(128) NOT NULL,
	user_id                 INT NOT NULL,
	account_signup_date     TIMESTAMP WITH TIME ZONE NOT NULL,

	CONSTRAINT fk_belongs_to FOREIGN KEY (user_id) REFERENCES Users(user_id) ON UPDATE CASCADE
 );


CREATE TABLE Restaurant (
	restaurant_id                   SERIAL PRIMARY KEY,
	restaurant_name                 VARCHAR(50) NOT NULL,
	restaurant_opening_time         TIME NOT NULL,
	restaurant_closing_time         TIME NOT NULL,

	CONSTRAINT ck_restaurant_time CHECK (restaurant_opening_time != restaurant_closing_time)
);

CREATE TABLE Customer (
	customer_balance        NUMERIC(8, 2) NOT NULL,
	currency CHAR(3)        DEFAULT 'MAD' NOT NULL,

	CONSTRAINT uq_customer_primary_key UNIQUE (user_id),
	CONSTRAINT ck_user_type CHECK (user_type IN ('Customer', 'Employee'))
) INHERITS (Users);

CREATE TABLE Employee (
	restaurant_id       INT NOT NULL,

	CONSTRAINT uq_employee_primary_key UNIQUE (user_id),
	CONSTRAINT ck_user_type CHECK (user_type IN ('Customer', 'Employee')),
	CONSTRAINT fk_employs FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE
) INHERITS (Users);

CREATE TABLE Category (
	category_id SERIAL PRIMARY KEY,
	category_name VARCHAR(50) NOT NULL
);

CREATE TABLE Product (
	product_id                  SERIAL PRIMARY KEY,
	restaurant_id               INT NOT NULL,
	category_id                 INT NOT NULL,
	product_name                VARCHAR(35) NOT NULL,
	product_price               NUMERIC(8, 2) NOT NULL,
	currency                    CHAR(3) DEFAULT 'MAD' NOT NULL,
	available_quantity          INT NOT NULL,
	min_quantity_allowed        INT NOT NULL,

	CONSTRAINT ck_currency CHECK (currency = 'MAD'),
	CONSTRAINT fk_offers FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_labels FOREIGN KEY (category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE
 );

CREATE TABLE Orders (
	order_id            SERIAL PRIMARY KEY,
	order_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
	order_type          CHAR(8) NOT NULL,
	order_total_value   NUMERIC(12, 2) NOT NULL,
	currency            CHAR(3) DEFAULT 'MAD' NOT NULL,
	order_status        VARCHAR(14) NOT NULL DEFAULT 'Pending',
	restaurant_id       INT NOT NULL,
	employee_id         INT NOT NULL,
	customer_id         INT NOT NULL,

	CONSTRAINT ck_order_timestamp CHECK (order_timestamp <= current_date),
	CONSTRAINT ck_currency CHECK (currency = 'MAD'),
	CONSTRAINT ck_order_date_time CHECK (order_type IN ('Pick-up', 'On-site')),
	CONSTRAINT ck_order_status CHECK (order_status IN ('Pending' , 'In Preparation', 'Canceled', 'Ready')),
	CONSTRAINT fk_receives FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE,
	CONSTRAINT fk_processes FOREIGN KEY (employee_id) REFERENCES Employee(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_places FOREIGN KEY (customer_id) REFERENCES Customer(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Order_Item (
	order_id            INT,
	product_id          INT,
	quantity_ordered    INT,
	product_unit_price          NUMERIC(12, 2) NOT NULL,
	total_item_value    NUMERIC(12, 2) NOT NULL,
	currency            CHAR(3) DEFAULT 'MAD' NOT NULL,

	PRIMARY KEY (order_id, product_id),
	CONSTRAINT uq_orderitem_primarykey UNIQUE (order_id, product_id),
	CONSTRAINT ck_currency CHECK (currency = 'MAD'),
	CONSTRAINT fk_contains FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_is_found_in FOREIGN KEY (product_id) REFERENCES Product(product_id) ON UPDATE CASCADE ON DELETE CASCADE
);

 -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

INSERT INTO Users (user_first_name, user_last_name, user_middle_names, user_type)
VALUES
('John', 'Doe', 'Michael', 'Customer'),
('Jane', 'Smith', NULL, 'Employee'),
('Carlos', 'Gomez', 'Luis Manuel', 'Customer'),
('Younes', 'Rahati', NULL, 'Customer'),
('Lahoucine', 'Chikry', NULL, 'Employee'),
('Mohammed', 'Rourou', 'Amine', 'Employee');

INSERT INTO Account (account_email, account_password, user_id, account_signup_date)
VALUES
('john.doe@example.com', 'hashedpassword1', 1, '2024-12-01 10:00:00+00'),
('jane.smith@example.com', 'hashedpassword2', 2, '2024-12-02 11:30:00+00'),
('carlos.gomez@example.com', 'hashedpassword3', 3, '2024-12-03 12:00:00+00'),
('younes.rahati@lilo.org', 'hashedpassword4', 4, '2024-12-04 9:30:00+00'),
('lahoucine@example.com', 'hashedpassword5', 5, '2024-12-5 8:00:00+00'),
('rourou@example.com', 'hashedpassword6', 6, '2024-12-6 7:00:00+00');

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

INSERT INTO Orders (order_timestamp, order_type, order_total_value, currency, order_status, restaurant_id, employee_id, customer_id)
VALUES
('2024-12-15 18:30:00+00', 'Pick-up', 60.00, 'MAD', 'Pending', 1, 2, 1),
('2024-12-15 19:00:00+00', 'On-site', 260.00, 'MAD', 'In Preparation', 2, 5, 3),
('2024-12-15 20:00:00+00', 'Pick-up', 370.00, 'MAD', 'Ready', 3, 6, 4);

INSERT INTO Order_Item (order_id, product_id, quantity_ordered, product_unit_price, total_item_value, currency)
VALUES
(1, 1, 2, 30.00, 60.00, 'MAD'),
(2, 5, 1, 200.00, 200.00, 'MAD'),
(2, 6, 1, 60.00, 60.00, 'MAD'),
(3, 7, 3, 25.00, 75.00, 'MAD'),
(3, 8, 2, 120.00, 240.00, 'MAD'),
(3, 9, 1, 55.00, 55.00, 'MAD');

--Check if a product can be ordered (new order_item)
CREATE OR REPLACE FUNCTION check_qoh() RETURNS TRIGGER
AS
$$
DECLARE
current_qoh int;
min_current_qoh int;
BEGIN
Current_qoh := (SELECT available_quantity FROM Product WHERE Product_id = NEW.Product_id);
min_current_qoh:= (SELECT min_quantity_allowed FROM product WHERE Product_id =
NEW.Product_id);
IF (current_qoh-NEW.quantity_ordered) < min_current_qoh THEN
	RAISE EXCEPTION 'Quantity on Hand for is less than the minimum quantity on hand';
END IF;
Return NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_check_qoh BEFORE INSERT ON Order_item
FOR EACH ROW EXECUTE Function check_qoh();

--Update Product Quantity After Inserting a New Order_item
CREATE OR REPLACE FUNCTION updateAvailableQuantity() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Product
SET available_quantity = avaialable_quantity - NEW.quantity_ordered
WHERE Product_id = NEW.Product_id;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateAvailableQuantity AFTER INSERT ON Order_item
FOR EACH ROW EXECUTE Function updateAvailableQuantity();

--Update Customer Balance
CREATE OR REPLACE FUNCTION updateCustomerBalance() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Customer
SET customer_balance = customer_balance - NEW.order_total_value
WHERE user_id = NEW.customer_id;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateCustomerBalance AFTER INSERT ON Orders
FOR EACH ROW EXECUTE Function updateCustomerBalance();

--Update Order Total Value
CREATE OR REPLACE FUNCTION updateOrderTotalValue() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Orders
SET Order_total_value = Order_total_value - NEW.total_item_value
WHERE order_id = NEW.order_id;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateOrderTotalValue AFTER INSERT ON Order_item
FOR EACH ROW EXECUTE Function updateOrderTotalValue();

--Update Total_item_value
CREATE OR REPLACE FUNCTION updateTotalItemValue() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Order_item
SET total_item_value = total_item_value - NEW.quantity_ordered * NEW.product_unit_price;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateTotalItemValue AFTER INSERT ON Order_item
FOR EACH ROW EXECUTE Function updateTotalItemValue();