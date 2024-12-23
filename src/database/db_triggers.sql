
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
SET available_quantity = available_quantity - NEW.quantity_ordered
WHERE Product_id = NEW.Product_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateAvailableQuantity AFTER INSERT ON Order_item
FOR EACH ROW
EXECUTE Function updateAvailableQuantity();

--Update Customer Balance
CREATE OR REPLACE FUNCTION updateCustomerBalance() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Customer
SET customer_balance = customer_balance - NEW.order_total_value
WHERE user_id = NEW.customer_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateCustomerBalance AFTER INSERT OR UPDATE ON Orders
FOR EACH ROW EXECUTE Function updateCustomerBalance();

--Update Order Total Value
CREATE OR REPLACE FUNCTION updateOrderTotalValue() RETURNS TRIGGER
AS
$$
BEGIN
UPDATE Orders
SET Order_total_value = Order_total_value + NEW.quantity_ordered * NEW.product_unit_price
WHERE order_id = NEW.order_id;
RETURN NEW;
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
SET total_item_value = total_item_value + NEW.quantity_ordered * NEW.product_unit_price
WHERE product_id = NEW.product_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_updateTotalItemValue AFTER INSERT ON Order_item
FOR EACH ROW EXECUTE Function updateTotalItemValue();