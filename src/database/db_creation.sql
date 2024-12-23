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
	account_signup_date     DATE NOT NULL,

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
	customer_balance        NUMERIC(8, 2) DEFAULT 5000 NOT NULL,
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
	order_timestamp     DATE NOT NULL,
	order_type          CHAR(8) NOT NULL,
	order_total_value   NUMERIC(12, 2) DEFAULT 0 NOT NULL,
	currency            CHAR(3) DEFAULT 'MAD' NOT NULL,
	order_status        VARCHAR(14) NOT NULL DEFAULT 'Pending',
	restaurant_id       INT NOT NULL,
	employee_id         INT,
	customer_id         INT NOT NULL,

	CONSTRAINT ck_order_timestamp CHECK (order_timestamp <= current_date),
	CONSTRAINT ck_currency CHECK (currency = 'MAD'),
	CONSTRAINT ck_order_type CHECK (order_type IN ('Pick-up', 'On-site')),
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
	total_item_value    NUMERIC(12, 2) DEFAULT 0 NOT NULL,
	currency            CHAR(3) DEFAULT 'MAD' NOT NULL,

	PRIMARY KEY (order_id, product_id),
	CONSTRAINT uq_orderitem_primarykey UNIQUE (order_id, product_id),
	CONSTRAINT ck_currency CHECK (currency = 'MAD'),
	CONSTRAINT fk_contains FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_is_found_in FOREIGN KEY (product_id) REFERENCES Product(product_id) ON UPDATE CASCADE ON DELETE CASCADE
);