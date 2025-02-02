package org.quickbitehub.authentication;

import org.quickbitehub.communicator.Emoji;

public enum AuthMessages {
	USER_ALREADY_SIGNED_IN(Emoji.ORANGE_CIRCLE.getCode() + " You are already signed in!"),
	USER_ALREADY_SIGNED_OUT(Emoji.ORANGE_CIRCLE.getCode() + " You are already signed out!"),

	EMAIL_PROMPT("Enter Email Address:"),
	PASSWORD_PROMPT("Enter Password:"),
	FIRST_NAME_PROMPT("Enter First Name:"),
	LAST_NAME_PROMPT("Enter Last Name"),
	MIDDLE_NAMES_PROMPT("Enter Middle Names(s):"),

	INVALID_EMAIL("Invalid Email Address"),
	EMAIL_ALREADY_EXIST("This Email Is Already Linked to Another Account"),
	EMAIL_DOES_NOT_EXIST("There Is No Such Email Registered"),
	INVALID_NAME("Invalid Name Format"),
	INVALID_MIDDLE_NAMES("Invalid Middle Name(s) Format"),

	SUCCESSFUL_SIGNIN(Emoji.GREEN_CIRCLE.getCode() + " You Signed In Successfully " + Emoji.HAND_WAVING.getCode()),
	FAILED_SIGNIN(Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode()),
	SUCCESSFUL_SIGNUP(Emoji.GREEN_CIRCLE.getCode() + " You Have Created Your Account Successfully " + Emoji.HAND_WAVING.getCode()),
	FAILED_SIGNUP(Emoji.RED_CIRCLE.getCode() + " *Sign Up Failed*\n Please Try Again Later " + Emoji.SAD_FACE.getCode()),
	SUCCESSFUL_SIGNOUT(Emoji.GREEN_CIRCLE.getCode() + " *_You have log out successfully. See you soon!_* " + Emoji.HAND_WAVING.getCode());


	private final String prompt;

	AuthMessages(String prompt) {
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}}
