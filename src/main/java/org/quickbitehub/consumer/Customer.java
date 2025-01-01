package org.quickbitehub.consumer;

import org.quickbitehub.authentication.DBCredentials;

import java.sql.*;
import java.util.HashMap;

public class Customer extends User {
	private Double customerBalance;
	private String currency = "MAD";
	private static HashMap<String, Customer> allCustomers = getAllCustomers(); // UserId -> Customer

	public Customer(String firstName, String lastName, String middleNames, String userId, String currency, Double customerBalance) {
		super(firstName, lastName, middleNames, UserType.CUSTOMER.getText(), userId);

		this.currency = currency;
		this.customerBalance = customerBalance;
	}

	public Customer(String firstName, String lastName, String middleNames) {
		super(firstName, lastName, middleNames, UserType.CUSTOMER.getText(), 0);
		this.customerBalance = 5000.00;

		allCustomers.put(this.USER_ID, this);
	}

	public static void fetchAllCustomers() {
		Customer.allCustomers = getAllCustomers();
	}

	public static Customer getCustomer(String customerId) {
		return allCustomers.get(customerId);
	}

	// Method to get all customers and store them in a HashMap
	public static HashMap<String, Customer> getAllCustomers() {
		String query = "SELECT * FROM Customer";
		HashMap<String, Customer> customers = new HashMap<>();  // HashMap to store customer data by customer_id

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
		     PreparedStatement statement = connection.prepareStatement(query);
		     ResultSet resultSet = statement.executeQuery()) {

			// Loop through the result set and add rows to the HashMap
			while (resultSet.next()) {
				String customerId = resultSet.getString("user_id");
				String firstName = resultSet.getString("user_first_name");
				String lastName = resultSet.getString("user_last_name");
				String middleNames = resultSet.getString("user_middle_names");
				Double balance = resultSet.getDouble("customer_balance");
				String currency = resultSet.getString("currency");

				Customer customerData = new Customer(firstName, lastName, middleNames, customerId, currency, balance);
				customers.put(customerId, customerData);
			}
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return customers;
	}


	public static String insertCustomer(String cus_fname, String cus_lname, double balance) {
		// Insert query for the Users table
		String userSQL = "INSERT INTO Users (user_first_name, user_last_name, user_type) VALUES (?, ?, ?) RETURNING user_id";

		// Insert query for the Customer table
		String insertSQL = "INSERT INTO Customer (user_id, user_first_name, user_last_name, customer_balance, user_type) VALUES (?, ?, ?, ?, ?)";
		int userId = 0;


		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try (Connection con = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {

			// First, insert into the Users table and get the generated user_id
			try (PreparedStatement preparedStatement = con.prepareStatement(userSQL)) {
				preparedStatement.setString(1, cus_fname);
				preparedStatement.setString(2, cus_lname);
				preparedStatement.setString(3, "Customer");

				// Execute the insert and get the user_id
				ResultSet rs = preparedStatement.executeQuery();
				if (rs.next()) {
					userId = rs.getInt("user_id");  // Get the generated user_id

					// Now, insert into the Customer table using the user_id
					try (PreparedStatement insertStatement = con.prepareStatement(insertSQL)) {
						insertStatement.setInt(1, userId);  // Use the generated user_id
						insertStatement.setString(2, cus_fname);
						insertStatement.setString(3, cus_lname);
						insertStatement.setDouble(4, balance);
						insertStatement.setString(5, "Customer");
						int rowsAffected = insertStatement.executeUpdate();
						System.out.println("Customer insert successful, rows affected: " + rowsAffected);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return String.valueOf(userId);
	}



	// Function to get the balance of a customer by ID
	public static double getCustomerBalance(String customerId) {
		// SQL query
		String query = "SELECT customer_balance FROM Customer WHERE user_id = ?";
		double balance = -1.00; // Default value if customer is not found


		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try (
				Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
				PreparedStatement statement = connection.prepareStatement(query)
		) {
			// Set the customer_id parameter in the query
			statement.setInt(1, Integer.valueOf(customerId));

			// Execute the query
			try (ResultSet rs = statement.executeQuery()) {

				if (rs.next()) {
					// Retrieve the balance from the result set
					balance = rs.getDouble("customer_balance");
				}

			}
		} catch (SQLException e) {
			// Handle SQL exception
			System.err.println("Database error in getCustomerBalance: " + e.getMessage());
		}


		return balance;
	}

}
