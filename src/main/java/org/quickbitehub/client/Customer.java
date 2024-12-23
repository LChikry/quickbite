package org.quickbitehub.client;

public class Customer extends User {
	private double customerBalance;
	private String currency = "MAD";

	public Customer(String firstName, String lastName, String middleNames) {
		super(firstName, lastName, middleNames, UserType.CUSTOMER.getText());
	}

}
