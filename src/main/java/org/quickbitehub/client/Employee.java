package org.quickbitehub.client;

public class Employee extends User {
	private String restaurantId;
	private Restaurant restaurant;

	public Employee(String firstName, String lastName, String middleNames) {
		super(firstName, lastName, middleNames, UserType.EMPLOYEE.getText());
	}
}
