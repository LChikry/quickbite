package org.quickbitehub.client;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.DBCredentials;

import java.sql.*;
import java.util.HashMap;

public class Employee extends User {
	private String restaurantId;
	public static HashMap<String, Employee> allEmployees = getAllEmployees(); // User_id -> Employee

	public Employee(String firstName, String lastName, String middleNames, String userId, String restaurantId) {
		super(firstName, lastName, middleNames, UserType.EMPLOYEE.getText(), userId);

		this.restaurantId = restaurantId;
	}

	public Employee(String firstName, String lastName, String middleNames, String restaurantId) {
		super(firstName, lastName, middleNames, UserType.EMPLOYEE.getText(), Integer.valueOf(restaurantId));
		this.restaurantId = restaurantId;

		allEmployees.put(this.USER_ID, this);
	}

	public static void fetchAllEmployees() {
		Employee.allEmployees = getAllEmployees();
	}

	public static Employee getEmployee(String employeeId) {
		return allEmployees.get(employeeId);
	}

	//Method to get all employees
	public static HashMap<String, Employee> getAllEmployees() {
		String query = "SELECT * FROM Employee";
		HashMap<String, Employee> employees = new HashMap<>();  // HashMap to store customer data by customer_id
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			assert DBCredentials.DB_URL.getDBInfo() != null;
			try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
			     PreparedStatement statement = connection.prepareStatement(query);
			     ResultSet resultSet = statement.executeQuery()) {

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					String employee_id = resultSet.getString("user_id");
					String firstName = resultSet.getString("user_first_name");
					String lastName = resultSet.getString("user_last_name");
					String middleNames = resultSet.getString("user_middle_names");
					String restaurantId = resultSet.getString("restaurant_id");

					Employee emp = new Employee(firstName, lastName, middleNames, employee_id, restaurantId);
					// Add this HashMap to the customers map using customer_id as the key
					employees.put(employee_id, emp);
				}

			}
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return employees;
	}


	//Method to insert into the employee table
	public static String insertEmployee(String emp_fname, String emp_lname, Integer res_id) {
		// Insert query for the Users table
		String userSQL = "INSERT INTO Users (user_first_name, user_last_name, user_type) VALUES (?, ?, ?) RETURNING user_id";

		// Insert query for the Customer table
		String insertSQL = "INSERT INTO Employee (user_id, user_first_name, user_last_name, restaurant_id, user_type) VALUES (?, ?, ?, ?, ?)";
		int userId = 0;

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try (Connection con = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {

			// First, insert into the Users table and get the generated user_id
			try (PreparedStatement preparedStatement = con.prepareStatement(userSQL)) {
				preparedStatement.setString(1, emp_fname);
				preparedStatement.setString(2, emp_lname);
				preparedStatement.setString(3, "Employee");

				// Execute the insert and get the user_id
				ResultSet rs = preparedStatement.executeQuery();
				if (rs.next()) {
					userId = rs.getInt("user_id");  // Get the generated user_id

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

		return String.valueOf(userId);
	}
}
