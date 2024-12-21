package org.quickbitehub.client;

public class User {
	private final String USER_ID;
	private String firstName;
	private String lastName;
	private String middleNames = "";
	private final UserType userType;

	public User(String userId, String firstName, String lastName, String middleNames, UserType userType) {
		this.USER_ID = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleNames = middleNames;
		this.userType = userType;
	}

	public User(String userId, String firstName, String lastName, UserType userType) {
		this.USER_ID = userId;
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