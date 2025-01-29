package org.quickbitehub.authentication;

public enum AuthPrompts {
	EMAIL("Enter Email Address:"),
	PASSWORD("Enter Password:"),
	FIRST_NAME("Enter First Name:"),
	LAST_NAME("Enter Last Name"),
	MIDDLE_NAMES("Enter Middle Names(s):");

	private final String prompt;

	AuthPrompts(String prompt) {
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}}
