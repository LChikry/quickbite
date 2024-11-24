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


DROP TABLE IF EXISTS Category;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Order;
DROP TABLE IF EXISTS Order_Item;

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
 productCode           varchar(10) PRIMARY KEY,
 description           varchar(35) not null,
 inDate                date not null,
 quantityOnHand        smallint not null,
 minQuantityOnHand     smallint not null,
 price                 decimal(8, 2),
 discountRate          decimal(2, 2) not null DEFAULT 0.00,
 vendorCode            int,
 CONSTRAINT CkProduct_InDate CHECK (inDate <= current_date),
 CONSTRAINT supplies FOREIGN KEY (vendorCode) REFERENCES Vendor(vendorCode)
                      ON UPDATE CASCADE
                      ON DELETE SET NULL
);

CREATE TABLE Order (

);

CREATE TABLE Order_Item (
 invoiceNumber int,
 lineNumber    smallint,
 productCode   varchar(10) not null,
 quantity      smallint not null DEFAULT 1,
 unitPrice     decimal(8, 2) not null,
 PRIMARY KEY (invoiceNumber, lineNumber),
 UNIQUE(invoiceNumber, productCode),
 CONSTRAINT contains FOREIGN KEY (invoiceNumber) REFERENCES Invoice(invoiceNumber)
                      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT is_found_in FOREIGN KEY (productCode) REFERENCES Product(productCode)
                          ON UPDATE CASCADE ON DELETE CASCADE
);

-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

-- Populate tables
