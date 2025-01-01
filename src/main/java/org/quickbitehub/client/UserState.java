package org.quickbitehub.client;

public enum UserState {
	AUTHENTICATION_PROCESS(""),
	DASHBOARD_PAGE("/start"),
	CANCEL_CURRENT_OPERATION("/cancel"),
	ISSUING_ORDER_PROCESS("/order"),
	CANCEL_PENDING_ORDER("/cancel_pending_order"),
	MANAGE_ORDERS_PAGE("/manage_orders"),
	CHOOSING_PRODUCTS(""),
	SETTINGS_PAGE("/settings"),
	LOGOUT("/logout"),
	HELP_PAGE("/help");

	private final String state;
	UserState(String state) {
		this.state = state;
	}

	private String getState() {
		return state;
	}

	public static UserState getValueOf(String name) throws IllegalArgumentException{
		if (name.equals(DASHBOARD_PAGE.getState())) return DASHBOARD_PAGE;
		if (name.equals(CANCEL_CURRENT_OPERATION.getState())) return CANCEL_CURRENT_OPERATION;
		if (name.equals(ISSUING_ORDER_PROCESS.getState())) return ISSUING_ORDER_PROCESS;
		if (name.equals(CANCEL_PENDING_ORDER.getState())) return CANCEL_PENDING_ORDER;
		if (name.equals(MANAGE_ORDERS_PAGE.getState())) return MANAGE_ORDERS_PAGE;
		if (name.equals(SETTINGS_PAGE.getState())) return SETTINGS_PAGE;
		if (name.equals(LOGOUT.getState())) return LOGOUT;
		if (name.equals(HELP_PAGE.getState())) return HELP_PAGE;

		return valueOf(name);
	}
}