package org.quickbitehub.order;

import org.quickbitehub.database.DBCredentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItem {
	private String orderId;
	private String productId;



	public static void insertOrderItem(Integer order_id, Integer product_id, Integer quantity, Double unit_price) throws SQLException {
		String insertSQL = "INSERT INTO Order_item (order_id, product_id, quantity_ordered, product_unit_price) VALUES (?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
		     PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

			preparedStatement.setInt(1, order_id);
			preparedStatement.setInt(2, product_id);
			preparedStatement.setInt(3, quantity);
			preparedStatement.setDouble(4, unit_price);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Insert successful, rows affected: " + rowsAffected);
		}
	}
}
