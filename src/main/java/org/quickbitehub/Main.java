package org.quickbitehub;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.quickbitehub.client.Account.getAllAccounts;


public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			QuickBite quickBiteBot = new QuickBite();
			botsApplication.registerBot(quickBiteBot.getBotToken(), quickBiteBot);

			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}


		//Inserting Customer
//		try {
//			insertCustomer("Hamza", "Amar", 9000.00);
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}

		//Inserting Employee
//		try {
//			insertEmployee("Wassim", "Amar", 1);
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}

		//Inserting Two Accounts (One Customer and One Employee)
//		try {
//			insertAccount("hamza@example.com", "securepassword123", 7, "2024-12-22");
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//
//		try {
//			insertAccount("wassim@example.com", "passwordverysecure", 8, "2024-12-23");
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}

		try {
			int var = insertOrder("2024-12-22", "Pick-up", 2, 4);
			System.out.println("The order ID is: "+ var);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		try {
			updateOrderStatus(4, "Ready");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		/*
		try {
			insertOrderItem(4, 5, 1, 200.00);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}*/


		//Returning All Employees
//		HashMap<Integer, HashMap<String, Object>> employees = getAllEmployees();
//
//		if (!employees.isEmpty()) {
//			for (HashMap.Entry<Integer, HashMap<String, Object>> entry : employees.entrySet()) {
//				int employee_id = entry.getKey();
//				HashMap<String, Object> employeeData = entry.getValue();
//				System.out.println("Employee ID: " + employee_id);
//				System.out.println("First Name: " + employeeData.get("emp_fname"));
//				System.out.println("Last Name: " + employeeData.get("emp_lname"));
//				System.out.println("Middle Name: " + employeeData.get("emp_middlename"));
//				System.out.println("User Type: " + employeeData.get("user_type"));
//				System.out.println("Restaurant ID: " + employeeData.get("res_id"));
//				System.out.println("-----------------------------");
//			}
//		} else {
//			System.out.println("No users found.");
//		}

		//Returning The Balance of Customer with ID = 4
		int customerId = 4;
		double res = getCustomerBalance(customerId);
		if (res != -1) {
			System.out.println("Customer balance: " + res);
		} else {
			System.out.println("Customer not found.");
		}


		//Returning All Customers
//		HashMap<Integer, HashMap<String, Object>> customers = getAllCustomers();
//
//		if (!customers.isEmpty()) {
//			for (HashMap.Entry<Integer, HashMap<String, Object>> entry : customers.entrySet()) {
//				customerId = entry.getKey();
//				HashMap<String, Object> customerData = entry.getValue();
//				System.out.println("Customer ID: " + customerId);
//				System.out.println("First Name: " + customerData.get("cus_fname"));
//				System.out.println("Last Name: " + customerData.get("cus_lname"));
//				System.out.println("Middle Name: " + customerData.get("cus_middlename"));
//				System.out.println("Balance: " + customerData.get("customer_balance"));
//				System.out.println("Currency: " + customerData.get("currency"));
//
//				System.out.println("-----------------------------");
//			}
//		} else {
//			System.out.println("No customers found.");
//		}


	}

	// Function to get the balance of a customer by ID
	public static double getCustomerBalance(int customerId) {
		// Database connection details
		String url = "jdbc:postgresql://localhost:5432/test2"; // Update with your database name
		String user = "postgres"; // Update with your username
		String password = ""; // Update with your password

		// SQL query
		String query = "SELECT customer_balance FROM Customer WHERE user_id = ?";
		double balance = -1.00; // Default value if customer is not found

		try (
				Connection connection = DriverManager.getConnection(url, user, password);
				PreparedStatement statement = connection.prepareStatement(query)
		) {
			// Set the customer_id parameter in the query
			statement.setInt(1, customerId);

			// Execute the query
			try (ResultSet rs = statement.executeQuery()) {

				if (rs.next()) {
					// Retrieve the balance from the result set
					balance = rs.getDouble("customer_balance");
				}

			}
		} catch (SQLException e) {
			// Handle SQL exception
			System.err.println("Database error: " + e.getMessage());
		}


		return balance;
	}






	//Method to get all accounts


	//Method to insert into the customer table



	//Method to insert into the order table
	public static int insertOrder(String order_timestamp, String order_type, Integer res_id, Integer cus_id) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "";

		// Insert query for the Users table
		String insertSQL = "INSERT INTO Orders (order_timestamp, order_type, restaurant_id, customer_id) VALUES (?, ?, ?, ?) RETURNING order_id";


		try (Connection con = DriverManager.getConnection(url, user, password)) {

			// Now, insert into the Customer table using the user_id
			try (PreparedStatement insertStatement = con.prepareStatement(insertSQL)) {

				// Parse the signupDate String to java.sql.Date (for DATE column)
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate localDate = LocalDate.parse(order_timestamp, formatter);
				java.sql.Date sqlTimestamp = Date.valueOf(localDate);

				insertStatement.setDate(1, sqlTimestamp);
				insertStatement.setString(2, order_type);
				insertStatement.setInt(3, res_id);
				insertStatement.setInt(4, cus_id);
				//insertStatement.setDouble(5, 0.00);

				// Execute the insert and get the user_id
				try (ResultSet rs = insertStatement.executeQuery()) {
					if (rs.next()) {
						return rs.getInt("order_id");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	//Method to insert Order_item
	/*public static void insertOrderItem(Integer order_id, Integer product_id, Integer quantity, Double unit_price) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12";
		String insertSQL = "INSERT INTO Order_item (order_id, product_id, quantity_ordered, product_unit_price) VALUES (?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(url, user, password);
		     PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

			preparedStatement.setInt(1, order_id);
			preparedStatement.setInt(2, product_id);
			preparedStatement.setInt(3, quantity);
			preparedStatement.setDouble(4, unit_price);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Insert successful, rows affected: " + rowsAffected);
		}
	}*/

	public static void updateOrderStatus(Integer order_id, String order_status) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "";

		// Update query for the Orders table
		String updateSQL = "UPDATE Orders SET order_status = ? WHERE order_id = ?";

		try (Connection con = DriverManager.getConnection(url, user, password);
		     PreparedStatement updateStatement = con.prepareStatement(updateSQL)) {

			updateStatement.setString(1, order_status);
			updateStatement.setInt(2, order_id);

			// Execute the update
			int rowsUpdated = updateStatement.executeUpdate();
			System.out.println(rowsUpdated + " rows updated.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}