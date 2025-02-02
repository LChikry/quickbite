package org.quickbitehub.authentication;

public enum AuthMessages {
	EMAIL_PROMPT("Enter Email Address:"),
	PASSWORD_PROMPT("Enter Password:"),
	FIRST_NAME_PROMPT("Enter First Name:"),
	LAST_NAME_PROMPT("Enter Last Name"),
	MIDDLE_NAMES_PROMPT("Enter Middle Names(s):");

	private final String prompt;

	AuthMessages(String prompt) {
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}}
