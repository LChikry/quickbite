package org.quickbitehub.app;

/* commands creation message to BotFather
start - Dashboard
cancel - Cancel Current Operation
order - Issue an Order
cancel_pending_order - Cancel a Pending Order
manage_orders - Manage Your Orders
settings - Configure Your Settings
signout - Sing Out
language - Change Interface Language
help - FAQ and Support
 */


import java.util.HashMap;

public enum UserState {
	__BEFORE_NEXT_UPDATE("__before_next_update"),
	AUTHENTICATION_NEEDED("/authenticate"),
	AUTHENTICATION_SIGNIN("/signin"),
	AUTHENTICATION_SIGNUP("/signup"),
	DASHBOARD_PAGE("/start"),
	CANCEL_CURRENT_OPERATION_WITH_NOTICE("/cancel"),
	__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE("__cancel_without_notice"),
	SELECT_FAVORITE_RESTAURANT("/order"),
	SEARCH_FOR_RESTAURANTS("/search_restaurant"),
	SEARCH_FOR_PRODUCTS("/search_products"),
	CONFIRM_ORDER("/confirm_order"),
	CANCEL_PENDING_ORDER("/cancel_pending_order"),
	MANAGE_ORDERS_PAGE("/manage_orders"),
	SETTINGS_PAGE("/settings"),
	CHANGE_EMAIL("/change_email"),
	CHANGE_PASSWORD("/change_password"),
	CHANGE_FAVORITE_RESTAURANTS("/change_favorite_restaurants"),
	CHANGE_LANGUAGE("/language"),
	CHANGE_FIRST_NAME("/change_first_name"),
	CHANGE_LAST_NAME("/change_last_name"),
	CHANGE_MIDDLE_NAMES("/change_middle_names"),
	AUTHENTICATION_SIGNOUT("/signout"),
	HELP_PAGE("/help"),
	__PREVIOUS_KEYBOARD("__back");

	private final String state;
	private static final HashMap<String, UserState> stateMap = new HashMap<>();

	static {
		for (UserState userState : UserState.values()) {
			stateMap.put(userState.state, userState);
		}
	}

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
				this == CANCEL_CURRENT_OPERATION_WITH_NOTICE ||
				this == __BEFORE_NEXT_UPDATE;
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
				this == SELECT_FAVORITE_RESTAURANT ||
				this == SEARCH_FOR_RESTAURANTS ||
				this == SEARCH_FOR_PRODUCTS ||
				this == CONFIRM_ORDER ||
				this == CANCEL_PENDING_ORDER ||
				this == AUTHENTICATION_SIGNOUT ||
				this == CHANGE_EMAIL ||
				this == CHANGE_PASSWORD ||
				this == CHANGE_FAVORITE_RESTAURANTS ||
				this == CHANGE_LANGUAGE ||
				this == CHANGE_FIRST_NAME ||
				this == CHANGE_LAST_NAME ||
				this == CHANGE_MIDDLE_NAMES ||
				this == __PREVIOUS_KEYBOARD;
	}

	public static UserState getValueOf(String name) {
		if (stateMap.get(name) != null) return stateMap.get(name);
		return valueOf(name);
	}
}