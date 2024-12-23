package org.quickbitehub.client;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;

public class Account implements Serializable {
	private final String ACCOUNT_ID;
	private final String USER_ID;
	private final String EMAIL;
	private final User USER;
	private final Instant ACCOUNT_SIGN_UP_DATE;
	private String password;
	private HashMap<Long, Boolean> isAuthenticated = new HashMap<>(); // TelegramId (device) to isAuthentication
	static public HashMap<String, Account> usersAccount = new HashMap<>(); // EMAIL to Account

	public Account(String EMAIL, String password, User USER, Long telegramId) {
		this.ACCOUNT_SIGN_UP_DATE = Instant.now();
		this.EMAIL = EMAIL;
		this.ACCOUNT_ID = ""; // db command
		this.USER = USER;
		this.USER_ID = USER.getUserId();
		this.password = password;
		this.isAuthenticated.put(telegramId, true);
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

	static public Account signUp(String email, String password, Long telegramId, String first_name, String last_name, String middle_names) {
		Customer customer = new Customer(first_name, last_name, middle_names);
		Account userAccount = new Account(email, password, customer, telegramId);

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

	public Instant getAccountSignUpDate() {
		return ACCOUNT_SIGN_UP_DATE;
	}

	public String getUserId() {
		return USER_ID;
	}
}