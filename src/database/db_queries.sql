CREATE VIEW vwRes1Menu AS SELECT * FROM Product WHERE restaurant_id = 1;

CREATE VIEW vwRes2Menu AS SELECT * FROM Product WHERE restaurant_id = 2;

CREATE VIEW vwRes3Menu AS SELECT * FROM Product WHERE restaurant_id = 3;

CREATE OR REPLACE FUNCTION fnOrdersGivenDate (d DATE)
RETURNS INTEGER
LANGUAGE PLPGSQL
AS
$$
DECLARE
    cnt INT;
BEGIN
    cnt := (SELECT COUNT(order_id)
            FROM Orders
            WHERE order_timestamp >= d
              AND order_timestamp < d + INTERVAL '1 day');
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

CREATE OR REPLACE FUNCTION get_order_total_value(ord_id int)
RETURNS NUMERIC(8, 2)
LANGUAGE PLPGSQL
AS $$
BEGIN
	RETURN (SELECT order_total_value FROM Orders WHERE order_id = ord_id);
END;
$$;