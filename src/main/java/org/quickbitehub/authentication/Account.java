package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.consumer.*;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.utils.LanguageType;
import org.quickbitehub.app.State;
import org.quickbitehub.app.UserState;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.io.Serializable;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Account implements Serializable {
	private final String ACCOUNT_ID;
	private final String USER_ID;
	private final User USER;
	private final LocalDate ACCOUNT_SIGN_UP_DATE;
	private String email;
	private String unformattedEmail;
	private String password;
	private LanguageType interfaceLanguage = LanguageType.ENGLISH;
	private final HashMap<Long, Boolean> isAuthenticated = new HashMap<>(); // TelegramId (device) to isAuthentication
	public static final int MAX_FAVORITE_RESTAURANT_LENGTH = 20;
	private final ArrayList<String> favoriteRestaurants = new ArrayList<>(MAX_FAVORITE_RESTAURANT_LENGTH); // recent used restaurantName in index 0
	public static HashMap<String, Account> usersAccount = getAllAccountsFromDB(); // email to Account

	public Account(String email, String unformattedEmail, String password, User USER, Long telegramId) {
		this.ACCOUNT_SIGN_UP_DATE = LocalDate.now();
		this.email = formatEmail(email);
		this.unformattedEmail = unformattedEmail.trim().strip();
		this.USER = USER;
		this.USER_ID = USER.getUserId();
		this.password = password;
		this.isAuthenticated.put(telegramId, true);

		insertAccountIntoDB(formatEmail(email), unformattedEmail, password, Integer.valueOf(USER.getUserId()), this.ACCOUNT_SIGN_UP_DATE);
		this.ACCOUNT_ID = Account.getAccountIdFromDB(email);
	}

	public Account(String email, String unformattedEmail, String password, String accountId, String userId, LocalDate signUpDate) {
		this.ACCOUNT_SIGN_UP_DATE = signUpDate;
		this.email = formatEmail(email);
		this.unformattedEmail = unformattedEmail.trim().strip();
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

	static public Account signUp(String email, String unformattedEmail, String password, Long telegramId, String first_name, String last_name, String middle_names, String userType, String restaurantId) {
		email = formatEmail(email);
		Customer customer = null;
		Employee employee = null;
		if (Objects.equals(userType, UserType.CUSTOMER.getText())) {
			customer = new Customer(first_name, last_name, middle_names);
		} else {
			employee = new Employee(first_name, last_name, middle_names, restaurantId);
		}
		Account userAccount;
		if (customer != null) userAccount = new Account(email, unformattedEmail, password, customer, telegramId);
		else userAccount = new Account(email, unformattedEmail, password, employee, telegramId);
		usersAccount.put(email, userAccount);
		return userAccount;
	}

	public boolean changeAccountPassword(Long telegramId, String oldPassword, String newPassword) {
		if (!this.isAuthenticated.get(telegramId) && oldPassword.equals(this.password)) return false;
		// task change it in db
		this.password = newPassword;
		return true;
	}

	public boolean changeAccountEmail(Long telegramId, String newEmail) {
		if (!this.isAuthenticated(telegramId)) return false;
		// task change it in db
		Account currentAccount = usersAccount.get(email);
		usersAccount.remove(email);
		this.email = formatEmail(newEmail);
		this.unformattedEmail = newEmail;
		usersAccount.put(email, currentAccount);
		return true;
	}

	static public String formatEmail(String email) {
		assert (Authentication.isEmailValid(email));
		String formattedEmail = email.strip().trim().toLowerCase();
		String username = formattedEmail.substring(0, formattedEmail.indexOf("@"));
		username = username.replace(".", "");
		if (username.contains("+")) username = username.substring(0, username.indexOf('+'));
		return username + formattedEmail.substring(formattedEmail.indexOf("@"));
	}

	static public String formatName(String name) {
		assert (name != null && !name.isBlank());
		String formattedName = name.strip().trim().toLowerCase();
		formattedName = formattedName.replaceAll("\\s{2,}", " ").trim();
		String[] words = formattedName.split(" ");
		StringBuilder formattedNameBuilder = new StringBuilder();
		for (String word : words) {
			formattedNameBuilder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(' ');
		}
		return formattedNameBuilder.toString();
	}

	public void logOut(Long telegramId) {this.isAuthenticated.put(telegramId, false);}
	static public boolean isAccountExist(String email) {return usersAccount.get(formatEmail(email)) != null;}
	public String getAccountId() {return ACCOUNT_ID;}
	public String getAccountEmail() {
		return email;
	}
	public String getUnformattedEmail() {return unformattedEmail;}
	public User getUser() {return USER;}
	public LocalDate getAccountSignUpDate() {return ACCOUNT_SIGN_UP_DATE;}
	public String getUserId() {return USER_ID;}
	public static String getUserId(String userEmail) {return usersAccount.get(formatEmail(userEmail)).getUserId();}
	public ArrayList<String> getFavoriteRestaurants() {return favoriteRestaurants;}
	public LanguageType getInterfaceLanguage() {return interfaceLanguage;}
	public void setInterfaceLanguage(LanguageType interfaceLanguage) {this.interfaceLanguage = interfaceLanguage;}
	public static void fetchAllAccounts() {Account.usersAccount = getAllAccountsFromDB();}

	public void addFavoriteRestaurant(String restaurantName) {
		if (!favoriteRestaurants.isEmpty() && favoriteRestaurants.getFirst().equals(restaurantName)) return;
		favoriteRestaurants.addFirst(restaurantName);
		while (favoriteRestaurants.size() > MAX_FAVORITE_RESTAURANT_LENGTH) {
			favoriteRestaurants.removeLast();
		}
	}

	public static void insertAccountIntoDB(String email, String unformattedEmail, String password, Integer userId, LocalDate signupDate) {
		String insertSQL = "INSERT INTO Account (account_email, unformatted_account_email, account_password, user_id, account_signup_date) VALUES (?, ?, ?, ?, ?)";

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
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, unformattedEmail);
			preparedStatement.setString(3, password);
			preparedStatement.setInt(4, userId);
			java.sql.Date sqlDate = java.sql.Date.valueOf(signupDate);
			preparedStatement.setDate(5, sqlDate);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Account Insert successful, rows affected: " + rowsAffected);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getAccountIdFromDB(String email) {
		email = formatEmail(email);
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

	public static HashMap<String, Account> getAllAccountsFromDB() {
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
				String email = formatEmail(resultSet.getString("account_email"));
				String unformattedEmail = resultSet.getString("unformatted_account_email");
				String password = resultSet.getString("account_password");
				String userId = String.valueOf(resultSet.getInt("user_id"));
				Date sqlDate = resultSet.getDate("account_signup_date");
				LocalDate signUpDate = sqlDate.toLocalDate();

				Account userAccount = new Account(email, unformattedEmail, password, account_id, userId, signUpDate);
				accounts.put(email, userAccount);
			}
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
		}
		return accounts;
	}
}