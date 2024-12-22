package org.quickbitehub;

import java.security.Key;

public enum KeyConstant {
	SIGNIN_EMAIL_MSG("___signin_email_msg"),
	SIGNIN_EMAIL_TXT("___signin_email_txt"),
	SIGNIN_PASSWORD_MSG("___signing_password_msg"),
	SIGNIN_PASSWORD_TXT("___signing_password_txt"),
	SIGNUP_EMAIL_MSG("___signup_email_msg"),
	SIGNUP_EMAIL_TXT("___signup_email_txt"),
	SIGNUP_PASSWORD_MSG("___signup_password_msg"),
	SIGNUP_PASSWORD_TXT("___signup_password_txt");

	private final String key;

	KeyConstant(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}