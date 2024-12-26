package org.quickbitehub;
import org.quickbitehub.client.Account;
import org.quickbitehub.client.Customer;
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

			Account.fetchAllAccounts();
			Customer.fetchAllCustomers();
			Customer cus = Customer.getCustomer(Account.getUserId("l.chikry@aui.ma"));
			System.out.println(Customer.getCustomerBalance(cus.getUserId()));


			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to get all customers and store them in a HashMap
	public static HashMap<Integer, HashMap<String, Object>> getAllCustomers() {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12"; // Your DB password

		String query = "SELECT * FROM Customer";
		HashMap<Integer, HashMap<String, Object>> customers = new HashMap<>();  // HashMap to store customer data by customer_id


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
		String password = "#Barakamon12"; // Your DB password

		String query = "SELECT * FROM Employee";
		HashMap<Integer, HashMap<String, Object>> employees = new HashMap<>();  // HashMap to store customer data by customer_id

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
		String password = "#Barakamon12"; // Your DB password

		String query = "SELECT * FROM Account";
		HashMap<Integer, HashMap<String, Object>> accounts = new HashMap<>();  // HashMap to store customer data by customer_id

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


	//Method to insert into the account table
	public static void insertAccount(String email, String pwd, Integer userId, String signupDate) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12";
		String insertSQL = "INSERT INTO Account (account_email, account_password, user_id, account_signup_date) VALUES (?, ?, ?, ?)";

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

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
		String password = "#Barakamon12";

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
		String password = "#Barakamon12";

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

	//Method to insert into the order table
	public static int insertOrder(String order_timestamp, String order_type, Integer res_id, Integer cus_id) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12";

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
	public static void insertOrderItem(Integer order_id, Integer product_id, Integer quantity, Double unit_price) throws SQLException {
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
	}

	public static void updateOrderStatus(Integer order_id, String order_status) throws SQLException {
		String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
		String user = "postgres"; // Your DB username
		String password = "#Barakamon12";

		// Update query for the Orders table
		String updateSQL = "CALL update_order_status(?, ?)";

		try (Connection con = DriverManager.getConnection(url, user, password);
		     PreparedStatement updateStatement = con.prepareStatement(updateSQL)) {

			updateStatement.setInt(1, order_id);
			updateStatement.setString(2, order_status);

			// Execute the update
			int rowsUpdated = updateStatement.executeUpdate();
			System.out.println(rowsUpdated + " rows updated.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}