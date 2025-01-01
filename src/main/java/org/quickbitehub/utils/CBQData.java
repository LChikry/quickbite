package org.quickbitehub.utils;

// Call Back Query Data
public enum CBQData {
	SIGNING_PROCESS("---signing_process"),
	SIGNUP_PROCESS("---signup_process"),
	ISSUE_ORDER("/order");

	private final String data;
	CBQData(String cbqData) {
		this.data = cbqData;
	}

	public String getData() {
		return data;
	}
}