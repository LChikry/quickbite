package org.quickbitehub.app;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.PageFactory;

import java.util.HashMap;
import java.util.Stack;

public class State {
	static final HashMap<Long, Stack<UserState>> userState = new HashMap<>(); // TelegramId -> State
	static final HashMap<Long, HashMap<Integer, UserState[]>> keyboardState = new HashMap<>(); // TelegramId -> keyboard MessageId -> previous if exists, current, next state if exists
	private static final int NUM_KEYBOARD_STATES = 3;

	public static void applyImmediateState(Long telegramId, UserState newState) {
		var states = userState.get(telegramId);
		UserState currentState = states.pop();
		states.push(newState);
		states.push(UserState.__BEFORE_NEXT_UPDATE);
		states.push(currentState);
	}
	public static void pushRequiredState(Long telegramId, UserState newState) {
		var states = userState.get(telegramId);
		UserState currentState = states.pop();
		states.push(newState);
		states.push(currentState);
	}
	static void pushImmediateState(Long telegramId, UserState recentState) {userState.get(telegramId).push(recentState);}
	public static boolean isStateless(Long telegramId) {
		return !userState.containsKey(telegramId) || userState.get(telegramId).isEmpty();
	}
	public static UserState getCurrentState(Long telegramId) {return userState.get(telegramId).peek();}

	public static void popAuthRelatedState(Long telegramId) {
		var states = userState.get(telegramId);
		while (!states.isEmpty() && states.peek().isStateAuthRelated()) {states.pop();}
		if (states.isEmpty()) {
			states.push(UserState.DASHBOARD_PAGE);
			states.push(UserState.__BEFORE_NEXT_UPDATE);
		}
	}

	static void navigateToProperState(Long telegramId, Integer messageId) {
		Stack<UserState> eventualState = userState.get(telegramId);
		assert (eventualState != null);
		if (eventualState.isEmpty()) {
			if (Authentication.isSessionAuthenticated(telegramId)) eventualState.push(UserState.DASHBOARD_PAGE);
			else eventualState.push(UserState.AUTHENTICATION_NEEDED);
		} else if (!Authentication.isSessionAuthenticated(telegramId) &&
				!eventualState.peek().isStateAuthRelated() &&
				!eventualState.peek().isImmediateState()) {
			eventualState.push(UserState.AUTHENTICATION_NEEDED);
		}

		UserState properState = eventualState.peek();
		switch (properState) {
			case AUTHENTICATION_NEEDED -> updateKeyboardState(telegramId, Authentication.authenticate(telegramId), properState);
			case AUTHENTICATION_SIGNIN -> Authentication.signIn(null, telegramId);
			case AUTHENTICATION_SIGNUP -> Authentication.signUp(null, telegramId);
			case DASHBOARD_PAGE -> updateKeyboardState(telegramId, PageFactory.viewDashboardPage(telegramId, messageId), properState);
			case CANCEL_CURRENT_OPERATION_WITH_NOTICE -> cancelCurrentOperation(telegramId, false);
			case __CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE -> cancelCurrentOperation(telegramId, true);
			case SELECT_FAVORITE_RESTAURANT -> PageFactory.viewFavoriteRestaurants(telegramId);
//			case SEARCH_FOR_RESTAURANTS ->
//			case SEARCH_FOR_PRODUCTS ->
//			case CONFIRM_ORDER ->
//			case CANCEL_PENDING_ORDER -> cancelPendingOrder();
//			case MANAGE_ORDERS_PAGE -> viewManageOrdersPage();
			case SETTINGS_PAGE -> updateKeyboardState(telegramId, PageFactory.viewSettingsPage(telegramId, messageId), properState);
			case AUTHENTICATION_SIGNOUT -> Authentication.signOut(telegramId);
			case HELP_PAGE -> updateKeyboardState(telegramId, PageFactory.viewHelpPage(telegramId, messageId), properState);
			case PREVIOUS_KEYBOARD -> gotoPreviousKeyboard(telegramId, messageId);
		}
		if (properState == UserState.AUTHENTICATION_SIGNOUT) eventualState.clear();
		else eventualState.pop();
	}

	private static void gotoPreviousKeyboard(Long telegramId, Integer messageId) {
		UserState properState = keyboardState.get(telegramId).get(messageId)[0];
		userState.get(telegramId).push(properState);
		navigateToProperState(telegramId, messageId);
	}


	private static void updateKeyboardState(Long telegramId, Integer msgId, UserState stateSent) {
		var keyboardsState = keyboardState.get(telegramId);
		if (msgId != null && !keyboardsState.isEmpty()) {
			for (var oldMsgId : keyboardsState.keySet()) {
				MessageHandler.deleteMessage(telegramId, oldMsgId);
			}
			keyboardsState.clear();
		}
		assert (keyboardsState.size() <= 1);
		if (msgId == null) msgId = keyboardsState.keySet().iterator().next();
		keyboardsState.clear();
		keyboardsState.put(msgId, getKeyboardChainState(stateSent));
	}

	private static UserState[] getKeyboardChainState(UserState currentState) {
		UserState[] possibleStates = new UserState[NUM_KEYBOARD_STATES];
		possibleStates[1] = currentState;

		switch (currentState) {
			case AUTHENTICATION_NEEDED, DASHBOARD_PAGE -> {
				possibleStates[0] = null;
				possibleStates[2] = null;
			}
			case SETTINGS_PAGE, HELP_PAGE -> {
				possibleStates[0] = UserState.DASHBOARD_PAGE;
				possibleStates[2] = null;
			}
			default -> System.err.println("You need to cover a keyboard chain state");
		}
		return possibleStates;
	}

	private static void cancelCurrentOperation(Long telegramId, boolean isNoticeSent) {
		Stack<UserState> states = userState.get(telegramId);
		if (!states.empty() && states.peek() == UserState.CANCEL_CURRENT_OPERATION_WITH_NOTICE) states.pop();
		String message;
		if (states.empty() || !states.peek().isOperationState()) {
			message = Emoji.ORANGE_CIRCLE.getCode() + " There is No Operation to Cancel\\, You are Good to Go " + Emoji.SMILING_FACE.getCode();
		} else {
			states.clear();
			message = Emoji.BLUE_CIRCLE.getCode() + " Current Operation is Canceled\\.";
		}
		if (!isNoticeSent) MessageHandler.sendText(telegramId, message, PageFactory.SHORT_DELAY_TIME_SEC);
	}

}
