CREATE VIEW vwRes1Menu AS SELECT * FROM Product WHERE restaurant_id = 1;

CREATE VIEW vwRes2Menu AS SELECT * FROM Product WHERE restaurant_id = 2;

CREATE VIEW vwRes3Menu AS SELECT * FROM Product WHERE restaurant_id = 3;

CREATE OR REPLACE FUNCTION fnOrdersGivenDate (d date)
RETURNS INTEGER
LANGUAGE PLPGSQL
AS
$$
DECLARE cnt int;
BEGIN
cnt := (SELECT COUNT(order_id) FROM Orders WHERE order_timestamp = d);
RETURN cnt;
END;
$$;

CREATE OR REPLACE PROCEDURE update_order_status(o_id int, status varchar(14))
LANGUAGE PLPGSQL
AS $$
BEGIN
UPDATE Orders
SET order_status = status
WHERE order_id = o_id;
END;
$$;