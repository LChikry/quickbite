package org.quickbitehub.consumer;

/* commands creation message to BotFather
start - Dashboard
cancel - Cancel Current Operation
order - Issue an Order
cancel_pending_order - Cancel a Pending Order
manage_orders - Manage Your Orders
settings - Configure Your Settings
signout - Sing Out
help - FAQ and Support
 */


public enum UserState {
	BEFORE_NEXT_UPDATE("_before_next_update"),
	AUTHENTICATION_NEEDED("/authenticate"),
	AUTHENTICATION_SIGNIN("/signin"),
	AUTHENTICATION_SIGNUP("/signup"),
	DASHBOARD_PAGE("/start"),
	CANCEL_CURRENT_OPERATION("/cancel"),
	ISSUE_ORDER("/order"),
	IO_RESTAURANT_SELECTION("/select_restaurant"),
	IO_PRODUCTS_SELECTION("/select_products"),
	IO_CONFIRMATION("/order_confirmation"),
	CANCEL_PENDING_ORDER("/cancel_pending_order"),
	MANAGE_ORDERS_PAGE("/manage_orders"),
	SETTINGS_PAGE("/settings"),
	AUTHENTICATION_SIGNOUT("/signout"),
	HELP_PAGE("/help");

	private final String state;
	UserState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	// Check if the new state is immediate and doesn't require authentication
	public boolean isImmediateState() {
		return this == HELP_PAGE ||
				this == AUTHENTICATION_SIGNOUT ||
				this == CANCEL_CURRENT_OPERATION ||
				this == BEFORE_NEXT_UPDATE;
	}

	// Check if the current stack's top state is not authentication-related
	public boolean isStateAuthRelated() {
		return this == AUTHENTICATION_NEEDED ||
				this == AUTHENTICATION_SIGNIN ||
				this == AUTHENTICATION_SIGNUP;
	}

	public boolean isOperationState() {
		return this == AUTHENTICATION_SIGNIN ||
				this == AUTHENTICATION_SIGNUP ||
				this == ISSUE_ORDER ||
				this == IO_RESTAURANT_SELECTION ||
				this == IO_PRODUCTS_SELECTION ||
				this == IO_CONFIRMATION ||
				this == CANCEL_PENDING_ORDER ||
				this == AUTHENTICATION_SIGNOUT;
	}

	public static UserState getValueOf(String name) throws IllegalArgumentException {
		if (name.equals(BEFORE_NEXT_UPDATE.getState())) return BEFORE_NEXT_UPDATE;
		if (name.equals(AUTHENTICATION_NEEDED.getState())) return AUTHENTICATION_NEEDED;
		if (name.equals(AUTHENTICATION_SIGNIN.getState())) return AUTHENTICATION_SIGNIN;
		if (name.equals(AUTHENTICATION_SIGNUP.getState())) return AUTHENTICATION_SIGNUP;
		if (name.equals(DASHBOARD_PAGE.getState())) return DASHBOARD_PAGE;
		if (name.equals(CANCEL_CURRENT_OPERATION.getState())) return CANCEL_CURRENT_OPERATION;
		if (name.equals(ISSUE_ORDER.getState())) return ISSUE_ORDER;
		if (name.equals(IO_RESTAURANT_SELECTION.getState())) return IO_RESTAURANT_SELECTION;
		if (name.equals(IO_PRODUCTS_SELECTION.getState())) return IO_PRODUCTS_SELECTION;
		if (name.equals(CANCEL_PENDING_ORDER.getState())) return CANCEL_PENDING_ORDER;
		if (name.equals(MANAGE_ORDERS_PAGE.getState())) return MANAGE_ORDERS_PAGE;
		if (name.equals(SETTINGS_PAGE.getState())) return SETTINGS_PAGE;
		if (name.equals(AUTHENTICATION_SIGNOUT.getState())) return AUTHENTICATION_SIGNOUT;
		if (name.equals(HELP_PAGE.getState())) return HELP_PAGE;

		return valueOf(name);
	}
}