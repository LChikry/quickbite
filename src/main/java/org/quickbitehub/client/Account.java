package org.quickbitehub.client;

import org.quickbitehub.authentication.DBCredentials;

import java.io.Serializable;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Objects;

public class Account implements Serializable {
	private final String ACCOUNT_ID;
	private final String USER_ID;
	private final String EMAIL;
	private final User USER;
	private final LocalDate ACCOUNT_SIGN_UP_DATE;
	private String password;
	private HashMap<Long, Boolean> isAuthenticated = new HashMap<>(); // TelegramId (device) to isAuthentication
	static public HashMap<String, Account> usersAccount = getAllAccounts(); // EMAIL to Account

	public Account(String EMAIL, String password, User USER, Long telegramId) {
		this.ACCOUNT_SIGN_UP_DATE = LocalDate.now();
		this.EMAIL = EMAIL;
		this.USER = USER;
		this.USER_ID = USER.getUserId();
		this.password = password;
		this.isAuthenticated.put(telegramId, true);

		insertAccount(EMAIL, password, Integer.valueOf(USER.getUserId()), this.ACCOUNT_SIGN_UP_DATE);
		this.ACCOUNT_ID = Account.getAccountIdFromDB(EMAIL);
	}

	public Account(String EMAIL, String password, String accountId, String userId, LocalDate signUpDate) {
		this.ACCOUNT_SIGN_UP_DATE = signUpDate;
		this.EMAIL = EMAIL;
		this.ACCOUNT_ID = accountId;
		this.USER_ID = userId;
		this.password = password;

		Customer cus = Customer.getCustomer(userId);
		if (cus != null) {
			this.USER = cus;
			return;
		}

		Employee emp = Employee.getEmployee(userId);
		this.USER = emp;
	}

	public static void fetchAllAccounts() {
		Account.usersAccount = getAllAccounts();
	}


	//Method to insert into the account table
	public static void insertAccount(String email, String pwd, Integer userId, LocalDate signupDate) {
		String insertSQL = "INSERT INTO Account (account_email, account_password, user_id, account_signup_date) VALUES (?, ?, ?, ?)";

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(),
				DBCredentials.DB_PASSWORD.getDBInfo());
		     PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

			// Parse the signupDate String to java.sql.Date (for DATE column)
			java.sql.Date sqlDate = java.sql.Date.valueOf(signupDate);
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//			LocalDate localDate = LocalDate.parse(signupDate, formatter);
//			java.sql.Date sqlDate = Date.valueOf(localDate);

			preparedStatement.setString(1, email);
			preparedStatement.setString(2, pwd);
			preparedStatement.setInt(3, userId);
			preparedStatement.setDate(4, sqlDate);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Account Insert successful, rows affected: " + rowsAffected);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Boolean isAuthenticated(Long telegramId) {
		return isAuthenticated.getOrDefault(telegramId, false);
	}

	static public Account authenticate(Long telegramId, String email, String password) {
		Account userAccount = usersAccount.get(email);
		if (userAccount == null) return null;
		if (!password.equals(userAccount.password)) return null;

		userAccount.isAuthenticated.put(telegramId, true);
		return userAccount;
	}

	static public Account signUp(String email, String password, Long telegramId, String first_name, String last_name, String middle_names, String userType, String restaurantId) {
		Customer customer = null;
		Employee employee = null;
		if (Objects.equals(userType, UserType.CUSTOMER.getText())) {
			customer = new Customer(first_name, last_name, middle_names);
		} else {
			employee = new Employee(first_name, last_name, middle_names, restaurantId);
		}
		Account userAccount;
		if (customer != null) {
			userAccount = new Account(email, password, customer, telegramId);
		} else {
			userAccount = new Account(email, password, employee, telegramId);
		}

		usersAccount.put(email, userAccount);
		return userAccount;
	}

	public void logOut(Long telegramId) {
		this.isAuthenticated.put(telegramId, false);
	}

	public boolean changeAccountPassword(Long telegramId, String oldPassword, String newPassword) {
		if (!this.isAuthenticated.get(telegramId) && oldPassword.equals(this.password)) return false;

		this.password = newPassword;
		return true;
	}

	static public boolean isValidEmail(String email) {
		return email.endsWith("@aui.ma");
	}

	static public boolean isAccountExist(String email) {
		return usersAccount.get(email.strip().trim().toLowerCase()) != null;
	}

	public String getAccountId() {
		return ACCOUNT_ID;
	}

	public String getAccountEmail() {
		return EMAIL;
	}

	public User getUser(Long telegramId) {
		if (!this.isAuthenticated.get(telegramId)) return null;

		return USER;
	}

	public LocalDate getAccountSignUpDate() {
		return ACCOUNT_SIGN_UP_DATE;
	}

	public String getUserId() {
		return USER_ID;
	}

	public static String getUserId(String userEmail) {
		return usersAccount.get(userEmail.trim().strip().toLowerCase()).getUserId();
	}


	public static String getAccountIdFromDB(String email) {
		String query = "SELECT account_id FROM Account WHERE account_email = ?;";
		String accountId = null;

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try (
				Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
						DBCredentials.DB_USER.getDBInfo(),
						DBCredentials.DB_PASSWORD.getDBInfo());
				PreparedStatement statement = connection.prepareStatement(query)) {
			// Set the customer_id parameter in the query
			statement.setString(1, email);

			// Execute the query
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					// Retrieve the balance from the result set
					accountId = rs.getString("account_id");
				}
			}
		} catch (SQLException e) {
			// Handle SQL exception
			System.err.println("Database error: " + e.getMessage());
		}

		return accountId;
	}

	public static HashMap<String, Account> getAllAccounts() {
		String query = "SELECT * FROM Account";
		HashMap<String, Account> accounts = new HashMap<>();  // HashMap to store customer data by customer_id


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
				// Create a HashMap for each row
				HashMap<String, Account> accountData = new HashMap<>();
				String account_id = resultSet.getString("account_id");
				String email = resultSet.getString("account_email");
				String password = resultSet.getString("account_password");
				String userId = String.valueOf(resultSet.getInt("user_id"));
				Date sqlDate = resultSet.getDate("account_signup_date");
				LocalDate signUpDate = sqlDate.toLocalDate();

				Account userAccount = new Account(email, password, account_id, userId, signUpDate);

				// Add this HashMap to the customers map using customer_id as the key
				accounts.put(email, userAccount);
			}

		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}

		return accounts;
	}
}