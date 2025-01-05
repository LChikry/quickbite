package org.quickbitehub.app;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.communicator.*;

import javax.print.attribute.standard.PageRanges;
import java.util.HashMap;
import java.util.Stack;

public class State {
	static final HashMap<Long, Stack<UserState>> userState = new HashMap<>(); // TelegramId -> State
	static final HashMap<Long, HashMap<Integer, UserState[]>> keyboardState = new HashMap<>(); // TelegramId -> keyboard MessageId -> previous if exists, current, next state if exists
	private static final int NUM_KEYBOARD_STATES = 2;

	public static void pushRequiredState(Long telegramId, UserState newState) {
		var states = userState.get(telegramId);
		UserState currentState = states.pop();
		states.push(newState);
		states.push(currentState);
	}
	static void pushImmediateState(Long telegramId, UserState recentState) {userState.get(telegramId).push(recentState);}
	public static void applyImmediateState(Long telegramId, UserState newState) {
		var states = userState.get(telegramId);
		UserState currentState = states.pop();
		states.push(newState);
		states.push(UserState.__BEFORE_NEXT_UPDATE);
		states.push(currentState);
	}
	public static boolean isUserStateless(Long telegramId) {
		return !userState.containsKey(telegramId) || userState.get(telegramId).isEmpty();
	}
	public static UserState getUserState(Long telegramId) {return userState.get(telegramId).peek();}
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
			case __PREVIOUS_KEYBOARD -> eventualState.push(keyboardState.get(telegramId).get(messageId)[0]);
			default -> MessageFactory.sendIncorrectOperationNotice(telegramId);
		}
		if (properState == UserState.AUTHENTICATION_SIGNOUT) eventualState.clear();
		else eventualState.pop();
	}
	private static void cancelCurrentOperation(Long telegramId, boolean isNoticeSent) {
		Stack<UserState> states = userState.get(telegramId);
		assert (states.peek() == UserState.CANCEL_CURRENT_OPERATION_WITH_NOTICE || states.peek() == UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE);
		UserState cancelType = states.pop(); // so that NavigateToProperState can pop the current state if it was not an op.
		String message;
		if (states.empty() || !states.peek().isOperationState()) {
			message = Emoji.ORANGE_CIRCLE.getCode() + " There is No Operation to Cancel\\, You are Good to Go " + Emoji.SMILING_FACE.getCode();
		} else {
			states.clear();
			message = Emoji.BLUE_CIRCLE.getCode() + " Current Operation Has Been Canceled\\.";
		}
		if (!isNoticeSent) MessageHandler.sendText(telegramId, message, TimeConstants.SHORT_DELAY_TIME_SEC.time());
	}
	private static void updateKeyboardState(Long telegramId, Integer msgId, UserState currentState) {
		var keyboardsState = keyboardState.get(telegramId);
		// we delete all keyboards in the chat if we are going to create new one
		if (msgId != null && !keyboardsState.isEmpty()) {
			for (var oldMsgId : keyboardsState.keySet()) {
				MessageHandler.deleteMessage(telegramId, oldMsgId, TimeConstants.NO_DELAY_TIME.time());
			}
		} else if (msgId == null) msgId = keyboardsState.keySet().iterator().next(); // otherwise, we take the id of existing one
		keyboardsState.clear();

		UserState[] possibleStates = new UserState[NUM_KEYBOARD_STATES];
		switch (currentState) {
			case AUTHENTICATION_NEEDED, DASHBOARD_PAGE -> {
				possibleStates[0] = null;
				possibleStates[1] = null;
			}
			case SETTINGS_PAGE, HELP_PAGE, MANAGE_ORDERS_PAGE -> {
				possibleStates[0] = UserState.DASHBOARD_PAGE;
				possibleStates[1] = null;
			}
			default -> System.err.println("You need to cover a keyboard chain state");
		}

		keyboardsState.put(msgId, possibleStates);
	}
}