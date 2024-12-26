package org.quickbitehub.order;

public enum OrderType {
	PICKUP("Pick-up"),
	ONSITE("On-site");

	private final String type;

	OrderType(String typeText) {
		this.type = typeText;
	}

	public String getType() {
		return this.type;
	}
}
