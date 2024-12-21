package org.quickbitehub.client;

import java.util.Stack;

public class User {
	private String firstName;
	private String lastName;
	private String middleNames = "";
	private final UserType userType;

	public User(String firstName, String lastName, String middleNames, UserType userType) {
        this.firstName = firstName;
        this.lastName = lastName;
		this.middleNames = middleNames;
		this.userType = userType;
	}

	public User(String firstName, String lastName, UserType userType) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userType = userType;
	}

	public String getUserFullName() {
		return lastName + ", " + firstName + " " + middleNames;
	}
}
