package org.quickbitehub.consumer;

public abstract class User {
	protected final String USER_ID;
	protected String firstName;
	protected String lastName;
	protected String middleNames = "";
	protected final String userType;


	public User(String firstName, String lastName, String middleNames, String userType, String userId) {
		this.USER_ID = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleNames = middleNames;
		this.userType = userType;
	}

	public User(String firstName, String lastName, String middleNames, String userType, Integer restaurantId) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleNames = middleNames;
		this.userType = userType;

		if (userType.equals(UserType.CUSTOMER.getText())) {
			this.USER_ID = Customer.insertCustomer(firstName, lastName, 5000.00);
		} else {
			this.USER_ID = Employee.insertEmployee(firstName, lastName, restaurantId);
		}
	}

	public String getUserFullName() {
		if (!middleNames.isBlank()) return firstName + " " + middleNames + " " + lastName;
		return firstName + " " + lastName;
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