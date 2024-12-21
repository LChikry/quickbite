package org.quickbitehub.client;

import java.time.Instant;

public class Account {
	private final Long ACCOUNT_ID;
	private final String EMAIL;
	private final String USER_ID;
	private final User USER;
	private final Instant ACCOUNT_SIGN_UP_DATE;
	private boolean isAuthenticated = false;
	private String password;

	public Account(String EMAIL, String password, Long ACCOUNT_ID, User USER) {
		this.ACCOUNT_SIGN_UP_DATE = Instant.now();
		this.EMAIL = EMAIL;
		this.ACCOUNT_ID = ACCOUNT_ID;
		this.USER = USER;
		this.USER_ID = USER.getUserId();
		this.password = password;
		this.isAuthenticated = true;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public boolean authenticate(String email, String password) {
		if (email.equals(this.EMAIL) && password.equals(this.password)) {
			this.isAuthenticated = true;
			return true;
		}

		return false;
	}

	public boolean logOut() {
		if (!this.isAuthenticated) return false;

		this.isAuthenticated = false;
		return true;
	}

	public boolean changeAccountPassword(String oldPassword, String newPassword) {
		if (!this.isAuthenticated && oldPassword.equals(this.password)) return false;

		this.password = newPassword;
		return true;
	}

	public Long getAccountId() {
		return ACCOUNT_ID;
	}

	public String getAccountEmail() {
		return EMAIL;
	}

	public User getUser() {
		if (!this.isAuthenticated) return null;

		return USER;
	}

	public Instant getAccountSignUpDate() {
		return ACCOUNT_SIGN_UP_DATE;
	}

	public String getUserId() {
		return USER_ID;
	}
}
