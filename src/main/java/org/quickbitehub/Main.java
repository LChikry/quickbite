package org.quickbitehub;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



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
		try {
			insertCustomer("Hamza", "Amar", 9000.00);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		//Inserting Employee
		try {
			insertEmployee("Wassim", "Amar", 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		//Inserting Two Accounts (One Customer and One Employee)
		try {
			insertAccount("hamza@example.com", "securepassword123", 7, "2024-12-22");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		try {
			insertAccount("wassim@example.com", "passwordverysecure", 8, "2024-12-23");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}


		//Returning All Employees
		HashMap<Integer, HashMap<String, Object>> employees = getAllEmployees();

		if (!employees.isEmpty()) {
			for (HashMap.Entry<Integer, HashMap<String, Object>> entry : employees.entrySet()) {
				int employee_id = entry.getKey();
				HashMap<String, Object> employeeData = entry.getValue();
				System.out.println("Employee ID: " + employee_id);
				System.out.println("First Name: " + employeeData.get("emp_fname"));
				System.out.println("Last Name: " + employeeData.get("emp_lname"));
				System.out.println("Middle Name: " + employeeData.get("emp_middlename"));
				System.out.println("User Type: " + employeeData.get("user_type"));
				System.out.println("Restaurant ID: " + employeeData.get("res_id"));
				System.out.println("-----------------------------");
			}
		} else {
			System.out.println("No users found.");
		}

		//Returning All Accounts
		HashMap<Integer, HashMap<String, Object>> accounts = getAllAccounts();

		if (!accounts.isEmpty()) {
			for (HashMap.Entry<Integer, HashMap<String, Object>> entry : accounts.entrySet()) {
				int account_id = entry.getKey();
				HashMap<String, Object> accountData = entry.getValue();
				System.out.println("Account ID: " + account_id);
				System.out.println("Email: " + accountData.get("email"));
				System.out.println("Password: " + accountData.get("password"));
				System.out.println("User ID: " + accountData.get("user_id"));
				System.out.println("Signup Date: " + accountData.get("signup_date"));
				System.out.println("-----------------------------");
			}
		} else {
			System.out.println("No accounts found.");
		}

		//Returning The Balance of Customer with ID = 4
		int customerId = 4;
		double res = getCustomerBalance(customerId);
		if (res != -1) {
			System.out.println("Customer balance: " + res);
		} else {
			System.out.println("Customer not found.");
		}


		//Returning All Customers
		HashMap<Integer, HashMap<String, Object>> customers = getAllCustomers();

		if (!customers.isEmpty()) {
			for (HashMap.Entry<Integer, HashMap<String, Object>> entry : customers.entrySet()) {
				customerId = entry.getKey();
				HashMap<String, Object> customerData = entry.getValue();
				System.out.println("Customer ID: " + customerId);
				System.out.println("First Name: " + customerData.get("cus_fname"));
				System.out.println("Last Name: " + customerData.get("cus_lname"));
				System.out.println("Middle Name: " + customerData.get("cus_middlename"));
				System.out.println("Balance: " + customerData.get("customer_balance"));
				System.out.println("Currency: " + customerData.get("currency"));

				System.out.println("-----------------------------");
			}
		} else {
			System.out.println("No customers found.");
		}


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


	// Method to get all customers and store them in a HashMap
	public static HashMap<Integer, HashMap<String, Object>> getAllCustomers() {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = ""; // Your DB password

		String query = "SELECT * FROM Customer";
		HashMap<Integer, HashMap<String, Object>> customers = new HashMap<>();  // HashMap to store customer data by customer_id

		try (Connection connection = DriverManager.getConnection(url, user, password);
		     PreparedStatement statement = connection.prepareStatement(query);
		     ResultSet resultSet = statement.executeQuery()) {

			// Loop through the result set and add rows to the HashMap
			while (resultSet.next()) {
				int customerId = resultSet.getInt("user_id");

				// Create a HashMap for each row
				HashMap<String, Object> customerData = new HashMap<>();
				customerData.put("cus_fname", resultSet.getString("user_first_name"));
				customerData.put("cus_lname", resultSet.getString("user_last_name"));
				customerData.put("cus_middlename", resultSet.getString("user_middle_names"));
				customerData.put("customer_balance", resultSet.getDouble("customer_balance"));
				customerData.put("currency", resultSet.getString("currency"));

				// Add this HashMap to the customers map using customer_id as the key
				customers.put(customerId, customerData);

			}

		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return customers;
	}

	//Method to get all employees
	public static HashMap<Integer, HashMap<String, Object>> getAllEmployees() {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = ""; // Your DB password

		String query = "SELECT * FROM Employee";
		HashMap<Integer, HashMap<String, Object>> employees = new HashMap<>();  // HashMap to store customer data by customer_id

		try (Connection connection = DriverManager.getConnection(url, user, password);
		     PreparedStatement statement = connection.prepareStatement(query);
		     ResultSet resultSet = statement.executeQuery()) {

			// Loop through the result set and add rows to the HashMap
			while (resultSet.next()) {
				int employee_id = resultSet.getInt("user_id");

				// Create a HashMap for each row
				HashMap<String, Object> employeeData = new HashMap<>();
				employeeData.put("emp_fname", resultSet.getString("user_first_name"));
				employeeData.put("emp_lname", resultSet.getString("user_last_name"));
				employeeData.put("emp_middlename", resultSet.getString("user_middle_names"));
				employeeData.put("res_id", resultSet.getInt("restaurant_id"));
				employeeData.put("user_type", resultSet.getString("user_type"));


				// Add this HashMap to the customers map using customer_id as the key
				employees.put(employee_id, employeeData);
			}

		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return employees;
	}

	//Method to get all accounts
	public static HashMap<Integer, HashMap<String, Object>> getAllAccounts() {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = ""; // Your DB password

		String query = "SELECT * FROM Account";
		HashMap<Integer, HashMap<String, Object>> accounts = new HashMap<>();  // HashMap to store customer data by customer_id

		try (Connection connection = DriverManager.getConnection(url, user, password);
		     PreparedStatement statement = connection.prepareStatement(query);
		     ResultSet resultSet = statement.executeQuery()) {

			// Loop through the result set and add rows to the HashMap
			while (resultSet.next()) {
				int account_id = resultSet.getInt("account_id");

				// Create a HashMap for each row
				HashMap<String, Object> accountData = new HashMap<>();
				accountData.put("email", resultSet.getString("account_email"));
				accountData.put("password", resultSet.getString("account_password"));
				accountData.put("user_id", resultSet.getInt("user_id"));
				accountData.put("signup_date", resultSet.getString("account_signup_date"));


				// Add this HashMap to the customers map using customer_id as the key
				accounts.put(account_id, accountData);
			}

		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return accounts;
	}


	//Method to insert into the account table
	public static void insertAccount(String email, String pwd, Integer userId, String signupDate) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "";
		String insertSQL = "INSERT INTO Account (account_email, account_password, user_id, account_signup_date) VALUES (?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(url, user, password);
		     PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

			// Parse the signupDate String to java.sql.Date (for DATE column)
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.parse(signupDate, formatter);
			java.sql.Date sqlDate = Date.valueOf(localDate);


			preparedStatement.setString(1, email);
			preparedStatement.setString(2, pwd);
			preparedStatement.setInt(3, userId);
			preparedStatement.setDate(4, sqlDate);


			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Insert successful, rows affected: " + rowsAffected);
		}
	}

	//Method to insert into the customer table
	public static void insertCustomer(String cus_fname, String cus_lname, double balance) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "";

		// Insert query for the Users table
		String userSQL = "INSERT INTO Users (user_first_name, user_last_name, user_type) VALUES (?, ?, ?) RETURNING user_id";

		// Insert query for the Customer table
		String insertSQL = "INSERT INTO Customer (user_id, user_first_name, user_last_name, customer_balance, user_type) VALUES (?, ?, ?, ?, ?)";

		try (Connection con = DriverManager.getConnection(url, user, password)) {

			// First, insert into the Users table and get the generated user_id
			try (PreparedStatement preparedStatement = con.prepareStatement(userSQL)) {
				preparedStatement.setString(1, cus_fname);
				preparedStatement.setString(2, cus_lname);
				preparedStatement.setString(3, "Customer");

				// Execute the insert and get the user_id
				ResultSet rs = preparedStatement.executeQuery();
				if (rs.next()) {
					int userId = rs.getInt("user_id");  // Get the generated user_id

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
	}

	//Method to insert into the employee table
	public static void insertEmployee(String emp_fname, String emp_lname, Integer res_id) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "";

		// Insert query for the Users table
		String userSQL = "INSERT INTO Users (user_first_name, user_last_name, user_type) VALUES (?, ?, ?) RETURNING user_id";

		// Insert query for the Customer table
		String insertSQL = "INSERT INTO Employee (user_id, user_first_name, user_last_name, restaurant_id, user_type) VALUES (?, ?, ?, ?, ?)";

		try (Connection con = DriverManager.getConnection(url, user, password)) {

			// First, insert into the Users table and get the generated user_id
			try (PreparedStatement preparedStatement = con.prepareStatement(userSQL)) {
				preparedStatement.setString(1, emp_fname);
				preparedStatement.setString(2, emp_lname);
				preparedStatement.setString(3, "Employee");

				// Execute the insert and get the user_id
				ResultSet rs = preparedStatement.executeQuery();
				if (rs.next()) {
					int userId = rs.getInt("user_id");  // Get the generated user_id

					// Now, insert into the Customer table using the user_id
					try (PreparedStatement insertStatement = con.prepareStatement(insertSQL)) {
						insertStatement.setInt(1, userId);  // Use the generated user_id
						insertStatement.setString(2, emp_fname);
						insertStatement.setString(3, emp_lname);
						insertStatement.setDouble(4, res_id);
						insertStatement.setString(5, "Employee");
						int rowsAffected = insertStatement.executeUpdate();
						System.out.println("Employee insert successful, rows affected: " + rowsAffected);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}