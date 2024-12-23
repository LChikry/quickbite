package org.quickbitehub;

// Call Back Query Data
public enum CBQData {
	SIGNING_MENU("---signing_menu"),
	SIGNING_VERIFICATION("---signing_verification"),
	SIGN_UP_MENU("---signup_menu");

	private final String data;
	CBQData(String cbqData) {
		this.data = cbqData;
	}

	public String getData() {
		return data;
	}
}