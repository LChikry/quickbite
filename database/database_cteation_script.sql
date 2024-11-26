/****************************************************************/
/* InvoicesDBINIT.sql						*/
/*								*/
/* This SQL script file creates the following tables:		*/
/*   Vendor, Product, Customer, Invoice, Line			*/
/* and loads the corresponding data rows.			*/
/*								*								*/
/****************************************************************/

-- Drop the tables if they exist
DROP TABLE IF EXISTS Order_Item;
DROP TABLE IF EXISTS Order;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Restaurant;
DROP TABLE IF EXISTS Customer;

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

-- Create the tables
CREATE TABLE Customer (
    customer_id     int PRIMARY KEY,
    cus_fname       varchar(15) not null,
    cus_lname       varchar(15) not null,
    cus_initial     char(1),
    cus_telegram_id     char(4),
    cus_telegram_username         char(8),
    cus_signup_date       decimal(8, 2) not null DEFAULT 0
);

CREATE TABLE Restaurant (
    vendorCode    int PRIMARY KEY,
    name          varchar(35) not null,
    contactPerson varchar(15) not null,
    areaCode      char(4) not null,
    phone         char(8) not null,
    country       char(2) not null,
    previousOrder char(1),
    CONSTRAINT CkVendor_PreviousOrder CHECK (previousOrder in ('Y', 'N'))
);

CREATE TABLE Employee (
    customer_id     int PRIMARY KEY,
    cus_fname       varchar(15) not null,
    cus_lname       varchar(15) not null,
    cus_initial     char(1),
    cus_telegram_id     char(4),
    cus_telegram_username         char(8),
    cus_signup_date       decimal(8, 2) not null DEFAULT 0
);

CREATE TABLE Category (

);


CREATE TABLE Product (
    product_id          VARCHAR(10) PRIMARY KEY,
    restaurant_id       INT NOT NULL,
    category_id         INT NOT NULL,
    product_name        VARCHAR(35) NOT NULL,
    price               NUMERIC(8, 2) NOT NULL,
    currency            CHAR(3) DEFAULT = 'MAD' NOT NULL,
    quantity            SMALLINT NOT NULL,
    min_quantity        SMALLINT,

    CONSTRAINT ck_currency CHECK (currency == 'MAD')
    CONSTRAINT fk_offers FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE ON DELETE CASCADE
    CONSTRAINT fk_labels FOREIGN KEY (category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Order (
    order_id            INT PRIMARY KEY,;
    order_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
    order_type          CHAR(8) NOT NULL,
    order_total_value   NUMERIC(12, 2) NOT NULL,
    currency            CHAR(3) DEFAULT = 'MAD' NOT NULL,
    order_status        VARCHAR(14) NOT NULL,
    restaurant_id       INT NOT NULL,
    cashier_id          INT NOT NULL,
    customer_id         INT NOT NULL,

    CONSTRAINT ck_order_timestamp CHECK (order_timestamp <= current_date),
    CONSTRAINT ck_currency CHECK (currency == 'MAD')
    CONSTRAINT ck_order_date_time CHECK (order_type IN ("Pick-up", "On-site")),
    CONSTRAINT fk_receives FOREIGN KEY (restaurant_id) REFERENCES Restaurant(restaurant_id) ON UPDATE CASCADE,
    CONSTRAINT fk_processes FOREIGN KEY (cashier_id) REFERENCES Cashier(cashier_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_places FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Order_Item (
    order_id            INT,
    product_id          INT,
    quantity_ordered    INT,
    unit_price          NUMERIC(12, 2) NOT NULL,
    total_item_value    NUMERIC(12, 2) NOT NULL,
    currency            CHAR(3) DEFAULT = 'MAD' NOT NULL,

    PRIMARY KEY (invoiceNumber, lineNumber),
    UNIQUE(order_id, product_id),
    CONSTRAINT ck_currency CHECK (currency == 'MAD')
    CONSTRAINT fk_contains FOREIGN KEY (order_id) REFERENCES Order(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_is_found_in FOREIGN KEY (product_id) REFERENCES Product(product_id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

-- Populate tables
