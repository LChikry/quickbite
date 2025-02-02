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
	__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE("__cancel_without_notice"),
	__PREVIOUS_KEYBOARD("__back"),

	DASHBOARD_PAGE("/start"),
	CANCEL_CURRENT_OPERATION_WITH_NOTICE("/cancel"),
	HELP_PAGE("/help"),

	AUTHENTICATION_PAGE("/authenticate"),
	SIGNOUT("/signout"),

	SIGNIN_PAGE("/signin"),
	__SET_SIGNIN_EMAIL("__set_signin_email"),
	__GET_SIGNIN_EMAIL("__get_signin_email"),
	__SET_SIGNIN_PASSWORD("__set_signin_password"),
	__GET_SIGNIN_PASSWORD("__get_signin_password"),
	__CONFIRM_SIGNIN("__confirm_signin"),

	SIGNUP_PAGE("/signup"),
	__SET_SIGNUP_EMAIL("__set_signup_email"),
	__GET_SIGNUP_EMAIL("__get_signup_email"),
	__SET_SIGNUP_PASSWORD("__set_signup_password"),
	__GET_SIGNUP_PASSWORD("__get_signup_password"),
	__SET_SIGNUP_FIRST_NAME("__set_signup_first_name"),
	__GET_SIGNUP_FIRST_NAME("__get_signup_first_name"),
	__SET_SIGNUP_LAST_NAME("__set_signup_last_name"),
	__GET_SIGNUP_LAST_NAME("__get_signup_last_name"),
	__SET_SIGNUP_MIDDLE_NAMES("__set_signup_middle_names"),
	__GET_SIGNUP_MIDDLE_NAMES("__get_signup_middle_names"),
	__CONFIRM_SIGNUP("__confirm_signup"),

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
	CHANGE_MIDDLE_NAMES("/change_middle_names");


	private final String state;
	private static final HashMap<String, UserState> stateMap = new HashMap<>();

	UserState(String state) {
		this.state = state;
	}

	static {
		for (UserState userState : UserState.values()) {
			stateMap.put(userState.state, userState);
		}
	}
	public static UserState getValueOf(String name) {
		if (stateMap.get(name) != null) return stateMap.get(name);
		return valueOf(name);
	}
	public String getState() {
		return state;
	}
	public UserState getOppositeAuthState() {
		return switch (this) {
			case __SET_SIGNIN_EMAIL -> __GET_SIGNIN_EMAIL;
			case __GET_SIGNIN_EMAIL -> __SET_SIGNIN_EMAIL;
			case __SET_SIGNIN_PASSWORD -> __GET_SIGNIN_PASSWORD;
			case __GET_SIGNIN_PASSWORD -> __SET_SIGNIN_PASSWORD;
			case __SET_SIGNUP_EMAIL -> __GET_SIGNUP_EMAIL;
			case __GET_SIGNUP_EMAIL -> __SET_SIGNUP_EMAIL;
			case __SET_SIGNUP_PASSWORD -> __GET_SIGNUP_PASSWORD;
			case __GET_SIGNUP_PASSWORD -> __SET_SIGNUP_PASSWORD;
			case __SET_SIGNUP_FIRST_NAME -> __GET_SIGNUP_FIRST_NAME;
			case __GET_SIGNUP_FIRST_NAME -> __SET_SIGNUP_FIRST_NAME;
			case __SET_SIGNUP_LAST_NAME -> __GET_SIGNUP_LAST_NAME;
			case __GET_SIGNUP_LAST_NAME -> __SET_SIGNUP_LAST_NAME;
			case __SET_SIGNUP_MIDDLE_NAMES -> __GET_SIGNUP_MIDDLE_NAMES;
			case __GET_SIGNUP_MIDDLE_NAMES -> __SET_SIGNUP_MIDDLE_NAMES;
			default -> null;
		};
	}

	public boolean isStateAuthRelated() {
		return this == AUTHENTICATION_PAGE ||
				this == SIGNIN_PAGE ||
				isSignInProcess() ||
				this == SIGNUP_PAGE ||
				isSignUpProcess();
	}
	public boolean isSignInProcess() {
		return 	this == __SET_SIGNIN_EMAIL ||
				this == __GET_SIGNIN_EMAIL ||
				this == __SET_SIGNIN_PASSWORD ||
				this == __GET_SIGNIN_PASSWORD ||
				this == __CONFIRM_SIGNIN;
	}
	public boolean isSignUpProcess() {
		return this == __SET_SIGNUP_EMAIL ||
				this == __GET_SIGNUP_EMAIL ||
				this == __SET_SIGNUP_PASSWORD ||
				this == __GET_SIGNUP_PASSWORD ||
				this == __SET_SIGNUP_FIRST_NAME ||
				this == __GET_SIGNUP_FIRST_NAME ||
				this == __SET_SIGNUP_LAST_NAME ||
				this == __GET_SIGNUP_LAST_NAME ||
				this == __SET_SIGNUP_MIDDLE_NAMES ||
				this == __GET_SIGNUP_MIDDLE_NAMES ||
				this == __CONFIRM_SIGNUP;
	}

	// Check if the new state is immediate and doesn't require authentication
	public boolean isImmediateState() {
		return this == HELP_PAGE ||
				this == SIGNOUT ||
				this == CANCEL_CURRENT_OPERATION_WITH_NOTICE ||
				this == __BEFORE_NEXT_UPDATE ||
				this == __PREVIOUS_KEYBOARD;
	}
	public boolean isPageCreationState() {
		return this == AUTHENTICATION_PAGE ||
				this == SIGNIN_PAGE ||
				this == SIGNUP_PAGE ||
				this == HELP_PAGE ||
				this == DASHBOARD_PAGE ||
				this == MANAGE_ORDERS_PAGE ||
				this == SETTINGS_PAGE ||
				this == CHANGE_LANGUAGE;
	}
	// task: this is outdated && update cancel current operation
	public boolean isOperationState() {
		return this == SIGNIN_PAGE ||
				this == SIGNUP_PAGE ||
				this == SELECT_FAVORITE_RESTAURANT ||
				this == SEARCH_FOR_RESTAURANTS ||
				this == SEARCH_FOR_PRODUCTS ||
				this == CONFIRM_ORDER ||
				this == CANCEL_PENDING_ORDER ||
				this == SIGNOUT ||
				this == CHANGE_EMAIL ||
				this == CHANGE_PASSWORD ||
				this == CHANGE_FAVORITE_RESTAURANTS ||
				this == CHANGE_LANGUAGE ||
				this == CHANGE_FIRST_NAME ||
				this == CHANGE_LAST_NAME ||
				this == CHANGE_MIDDLE_NAMES ||
				this == __PREVIOUS_KEYBOARD;
	}
}