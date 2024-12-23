package org.quickbitehub;

public enum KeyConstant {
	SIGNING_EMAIL_MSG("___signing_email_msg"),
	SIGNING_EMAIL_TXT("___signing_email_txt"),
	SIGNING_PASSWORD_MSG("___signing_password_msg"),
	SIGNING_PASSWORD_TXT("___signing_password_txt"),
	SIGNUP_EMAIL_MSG("___signup_email_msg"),
	SIGNUP_EMAIL_TXT("___signup_email_txt"),
	SIGNUP_PASSWORD_MSG("___signup_password_msg"),
	SIGNUP_PASSWORD_TXT("___signup_password_txt"),
	SIGNUP_ID_MSG("___signup_id_msg"),
	SIGNUP_ID_TXT("___signup_id_txt"),
	SIGNUP_FIRST_NAME_MSG("___signup_first_name_msg"),
	SIGNUP_FIRST_NAME_TXT("___signup_first_name_txt"),
	SIGNUP_LAST_NAME_MSG("___signup_last_name_msg"),
	SIGNUP_LAST_NAME_TXT("___signup_last_name_txt"),
	SIGNUP_MIDDLE_NAMES_MSG("___signup_middle_name_msg"),
	SIGNUP_MIDDLE_NAMES_TXT("___signup_middle_name_txt");

	private final String key;

	KeyConstant(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}