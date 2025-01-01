package org.quickbitehub.consumer;

public enum UserType {
	CUSTOMER("Customer"),
	EMPLOYEE("Employee");

	private final String name;

	UserType(String text) {
		this.name = text;
	}

	public String getText() {
		return name;
	}
}
