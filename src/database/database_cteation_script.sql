-- Drop the tables if they exist
DROP TABLE IF EXISTS Order_Item;
DROP TABLE IF EXISTS Order;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Restaurant;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS Account;

------------------------------------------
CREATE TABLE users (
 	user_id         INT PRIMARY KEY,
     first_name      VARCHAR(15) NOT NULL,
     last_name       VARCHAR(25) NOT NULL,
     middle_names    VARCHAR(40),
     user_type       VARCHAR(10) NOT NULL
 );

 CREATE TABLE Account (
     account_id              INT PRIMARY KEY,
     email                   VARCHAR(100) NOT NULL,
     password                VARCHAR(128) NOT NULL,
     user_id                 INT NOT NULL,
     telegram_id             INT NOT NULL,
     telegram_username       VARCHAR(50) NOT NULL,
     signup_date             TIMESTAMP WITH TIME ZONE NOT NULL,

     CONSTRAINT fk_belongs_to FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE
 );


 CREATE TABLE Customer (
     customer_balance NUMERIC(8, 2) NOT NULL,
     currency CHAR(3) DEFAULT 'MAD' NOT NULL
 ) INHERITS (users);


 CREATE TABLE Restaurant (
     restaurant_id INT PRIMARY KEY
 );


 CREATE TABLE Employee (
     restaurant_id       INT NOT NULL,
     CONSTRAINT fk_employs FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE
 ) INHERITS (users);


 CREATE TABLE Category (
 	category_id INT PRIMARY KEY,
 	category_name VARCHAR(50) NOT NULL
 );


 CREATE TABLE Product (
     product_id          VARCHAR(10) PRIMARY KEY,
     restaurant_id       INT NOT NULL,
     category_id         INT NOT NULL,
     product_name        VARCHAR(35) NOT NULL,
     price               NUMERIC(8, 2) NOT NULL,
     currency            CHAR(3) DEFAULT 'MAD' NOT NULL,
     quantity            INT NOT NULL,
     min_quantity        INT,

     CONSTRAINT ck_currency CHECK (currency = 'MAD'),
     CONSTRAINT fk_offers FOREIGN KEY (restaurant_id)
 		REFERENCES Restaurant(restaurant_id)
 		ON UPDATE CASCADE
 		ON DELETE CASCADE,
     CONSTRAINT fk_labels FOREIGN KEY (category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE
 );

 CREATE TABLE orders (
     order_id            INT PRIMARY KEY,
     order_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
     order_type          CHAR(8) NOT NULL,
     order_total_value   NUMERIC(12, 2) NOT NULL,
     currency            CHAR(3) DEFAULT 'MAD' NOT NULL,
     order_status        VARCHAR(14) NOT NULL DEFAULT 'Pending',
     restaurant_id       INT NOT NULL,
     employee_id          INT NOT NULL,
     customer_id         INT NOT NULL,

     CONSTRAINT ck_order_timestamp CHECK (order_timestamp <= current_date),
     CONSTRAINT ck_currency CHECK (currency = 'MAD'),
     CONSTRAINT ck_order_date_time CHECK (order_type IN ('Pick-up', 'On-site')),
     CONSTRAINT ck_order_status CHECK (order_status IN ('Pending' , 'In Preparation', 'Canceled', 'Ready')),
     CONSTRAINT fk_receives FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE,
     CONSTRAINT fk_processes FOREIGN KEY (employee_id) REFERENCES Employee(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
     CONSTRAINT fk_places FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON UPDATE CASCADE ON DELETE CASCADE
 );

 CREATE TABLE Order_Item (
     order_id            INT,
     product_id          INT,
     quantity_ordered    INT,
     unit_price          NUMERIC(12, 2) NOT NULL,
     total_item_value    NUMERIC(12, 2) NOT NULL,
     currency            CHAR(3) DEFAULT 'MAD' NOT NULL,

     PRIMARY KEY (invoiceNumber, lineNumber),
     UNIQUE(order_id, product_id),
     CONSTRAINT ck_currency CHECK (currency = 'MAD'),
     CONSTRAINT fk_contains FOREIGN KEY (order_id) REFERENCES orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
     CONSTRAINT fk_is_found_in FOREIGN KEY (product_id) REFERENCES Product(product_id) ON UPDATE CASCADE ON DELETE CASCADE
 );

 -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

 -- Populate tables
