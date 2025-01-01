package org.quickbitehub.client;

public enum UserState {
	AUTHENTICATION_PROCESS(""),
	ISSUING_ORDER_PROCESS("/order"),
	CANCEL_PENDING_ORDER("/cancel"),
	MANAGE_ORDERS_PAGE("/manage_orders"),
	CHOOSING_PRODUCTS(""),
	DASHBOARD_PAGE("/start"),
	SETTINGS_PAGE("/settings");

	private final String state;
	UserState(String state) {
		this.state = state;
	}

	private String getStateText() {
		return state;
	}

	public static UserState getValueOf(String name) throws IllegalArgumentException{
		if (name.equals(ISSUING_ORDER_PROCESS.getStateText())) return ISSUING_ORDER_PROCESS;
		if (name.equals(CANCEL_PENDING_ORDER.getStateText())) return CANCEL_PENDING_ORDER;
		if (name.equals(MANAGE_ORDERS_PAGE.getStateText())) return MANAGE_ORDERS_PAGE;
		if (name.equals(DASHBOARD_PAGE.getStateText())) return DASHBOARD_PAGE;
		if (name.equals(SETTINGS_PAGE.getStateText())) return SETTINGS_PAGE;

		return valueOf(name);
	}
}