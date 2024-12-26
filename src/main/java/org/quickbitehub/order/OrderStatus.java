package org.quickbitehub.order;

public enum OrderStatus {
	PENDING("Pending"),
	IN_PREPARATION("In Preparation"),
	CANCELED("Canceled"),
	READY("Ready");

	private final String status;

	OrderStatus(String statusText) {
		this.status = statusText;
	}

	public String getStatus() {
		return this.status;
	}
}
