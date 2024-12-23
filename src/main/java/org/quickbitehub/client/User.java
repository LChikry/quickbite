package org.quickbitehub.client;

public abstract class User {
	protected final String USER_ID;
	protected String firstName;
	protected String lastName;
	protected String middleNames = "";
	protected final String userType;

	public User(String firstName, String lastName, String middleNames, String userType) {
		this.USER_ID = "sdkjfdskfj"; // db command
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleNames = middleNames;
		this.userType = userType;
	}

	public User(String firstName, String lastName, String userType) {
		this.USER_ID = "fsdkflsdkf"; // db command
		this.firstName = firstName;
		this.lastName = lastName;
		this.userType = userType;
	}

	public String getUserFullName() {
		return lastName + ", " + firstName + " " + middleNames;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMiddleNames() {
		return middleNames;
	}

	public String getUserId() {
		return USER_ID;
	}
}