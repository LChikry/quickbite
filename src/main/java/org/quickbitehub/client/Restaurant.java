package org.quickbitehub.client;

import org.quickbitehub.authentication.DBCredentials;

import java.sql.*;
import java.time.OffsetTime;
import java.util.HashMap;

public class Restaurant {
	private final String RESTAURANT_ID;
	private String restaurantName;
	private HashMap<DaysOfWeek, OffsetTime> restaurantOpeningTime;
	private HashMap<DaysOfWeek, OffsetTime> restaurantClosingTime;

	public Restaurant(String restaurantId) {
		RESTAURANT_ID = restaurantId;
	}

	public String getRestaurantId() {
		return RESTAURANT_ID;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		// task: change it in the db
		this.restaurantName = restaurantName;
	}

	public static HashMap<Integer, HashMap<String, Object>> getRestaurantMenu(Integer res_id) {
		String query;
		HashMap<Integer, HashMap<String, Object>> menu = new HashMap<>();

		if(res_id==1) {
			query = "SELECT * FROM vwRes1Menu";

			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
					DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
			     PreparedStatement statement = connection.prepareStatement(query);
			     ResultSet resultSet = statement.executeQuery()) {

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					int product_id = resultSet.getInt("product_id");

					// Create a HashMap for each row
					HashMap<String, Object> menuData = new HashMap<>();
					menuData.put("restaurant_id", resultSet.getInt("restaurant_id"));
					menuData.put("category_id", resultSet.getInt("category_id"));
					menuData.put("product_name", resultSet.getString("product_name"));
					menuData.put("product_price", resultSet.getDouble("product_price"));
					menuData.put("currency", resultSet.getString("currency"));
					menuData.put("available_quantity", resultSet.getInt("available_quantity"));
					menuData.put("min_quantity_allowed", resultSet.getInt("min_quantity_allowed"));

					// Add this HashMap to the customers map using customer_id as the key
					menu.put(product_id, menuData);
				}

			} catch (SQLException e) {
				System.err.println("Database error: " + e.getMessage());
			}
		} else if (res_id==2) {
			query = "SELECT * FROM vwRes2Menu";


			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			try (Connection connection = DriverManager.getConnection(url, user, password);
			     PreparedStatement statement = connection.prepareStatement(query);
			     ResultSet resultSet = statement.executeQuery()) {

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					int product_id = resultSet.getInt("product_id");

					// Create a HashMap for each row
					HashMap<String, Object> menuData = new HashMap<>();
					menuData.put("restaurant_id", resultSet.getInt("restaurant_id"));
					menuData.put("category_id", resultSet.getInt("category_id"));
					menuData.put("product_name", resultSet.getString("product_name"));
					menuData.put("product_price", resultSet.getDouble("product_price"));
					menuData.put("currency", resultSet.getString("currency"));
					menuData.put("available_quantity", resultSet.getInt("available_quantity"));
					menuData.put("min_quantity_allowed", resultSet.getInt("min_quantity_allowed"));

					// Add this HashMap to the customers map using customer_id as the key
					menu.put(product_id, menuData);
				}

			} catch (SQLException e) {
				System.err.println("Database error: " + e.getMessage());
			}
		} else if (res_id==3) {
			query = "SELECT * FROM vwRes3Menu";


			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}


			try (Connection connection = DriverManager.getConnection(url, user, password);
			     PreparedStatement statement = connection.prepareStatement(query);
			     ResultSet resultSet = statement.executeQuery()) {

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					int product_id = resultSet.getInt("product_id");

					// Create a HashMap for each row
					HashMap<String, Object> menuData = new HashMap<>();
					menuData.put("restaurant_id", resultSet.getInt("restaurant_id"));
					menuData.put("category_id", resultSet.getInt("category_id"));
					menuData.put("product_name", resultSet.getString("product_name"));
					menuData.put("product_price", resultSet.getDouble("product_price"));
					menuData.put("currency", resultSet.getString("currency"));
					menuData.put("available_quantity", resultSet.getInt("available_quantity"));
					menuData.put("min_quantity_allowed", resultSet.getInt("min_quantity_allowed"));

					// Add this HashMap to the customers map using customer_id as the key
					menu.put(product_id, menuData);
				}

			} catch (SQLException e) {
				System.err.println("Database error: " + e.getMessage());
			}
		}

		return menu;
	}
}
