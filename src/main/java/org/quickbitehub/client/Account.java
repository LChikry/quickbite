package org.quickbitehub.client;

public class Account {
	private String email;
	private String password;
	private final Long ACCOUNT_ID;
	private boolean isAuthenticated = false;
	private final User USER;

	public Account(String email, String password, Long ACCOUNT_ID, User USER) {
		this.email = email;
		this.password = password;
		this.ACCOUNT_ID = ACCOUNT_ID;
		this.isAuthenticated = true;
		this.USER = USER;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public boolean authenticate(String email, String password) {
		if (email.equals(this.email) && password.equals(this.password)) {
			this.isAuthenticated = true;
			return true;
		}

		return false;
	}

	public boolean logout() {
		if (!this.isAuthenticated) return false;

		this.isAuthenticated = false;
		return true;
	}

	public Long getAccountId() {
		return ACCOUNT_ID;
	}

	public String getAccountEmail() {
		return email;
	}

	public boolean changeAccountEmail(String oldEmail, String newEmail) {
		if (!this.isAuthenticated && oldEmail.equals(this.email)) return false;

		this.email = newEmail;
		return true;
	}

	public boolean changeAccountPassword(String oldPassword, String newPassword) {
		if (!this.isAuthenticated && oldPassword.equals(this.password)) return false;

		this.password = newPassword;
		return true;
	}

	public User getUser() {
		if (!this.isAuthenticated) return null;

		return USER;
	}
}
