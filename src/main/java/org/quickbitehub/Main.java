package org.quickbitehub;

import org.quickbitehub.authentication.Account;
import org.quickbitehub.client.Customer;
import org.quickbitehub.client.Employee;
import org.quickbitehub.order.Order;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;
import java.util.HashMap;


public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			QuickBite quickBiteBot = new QuickBite();
			Account.fetchAllAccounts();
			Customer.fetchAllCustomers();
			Employee.fetchAllEmployees();
			Order.fetchAllCurrentOrders();
			Order.fetchAllPast30DayOrders();
			botsApplication.registerBot(quickBiteBot.getBotToken(), quickBiteBot);

			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static HashMap<Integer, HashMap<String, Object>> getRestaurantMenu(Integer res_id) {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12"; // Your DB password
		String query;
		HashMap<Integer, HashMap<String, Object>> menu = new HashMap<>();

		if(res_id==1) {
			query = "SELECT * FROM vwRes1Menu";


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