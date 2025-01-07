package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.communicator.*;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.Stack;

public class State {
	static final HashMap<Long, Stack<Pair<UserState, Integer>>> userState = new HashMap<>(); // TelegramId -> State
	static final HashMap<Long, HashMap<Integer, UserState[]>> keyboardState = new HashMap<>(); // TelegramId -> keyboard MessageId -> previous if exists, current, next state if exists
	private static final int NUM_KEYBOARD_STATES = 2;

	public static void pushRequiredState(Long telegramId, Pair<UserState, Integer> newState) {
		var states = userState.get(telegramId);
		Pair<UserState, Integer> currentState = states.pop();
		states.push(newState);
		states.push(currentState);
	}
	static void pushImmediateState(Long telegramId, Pair<UserState, Integer> recentState) {userState.get(telegramId).push(recentState);}
	public static void applyImmediateState(Long telegramId, Pair<UserState, Integer> newState) {
		var states = userState.get(telegramId);
		Pair<UserState, Integer> currentState = states.pop();
		states.push(newState);
		states.push(Pair.of(UserState.__BEFORE_NEXT_UPDATE, null));
		states.push(currentState);
	}
	public static boolean isUserStateless(Long telegramId) {
		return !userState.containsKey(telegramId) || userState.get(telegramId).isEmpty();
	}
	public static UserState getUserState(Long telegramId) {return userState.get(telegramId).peek().getLeft();}
	public static void popAuthRelatedState(Long telegramId) {
		var states = userState.get(telegramId);
		while (!states.isEmpty() && states.peek().getLeft().isStateAuthRelated()) {
			if (states.peek().getRight() != null) {
				MessageHandler.deleteMessage(telegramId, states.pop().getRight(), TimeConstants.NO_DELAY_TIME.time());
			} else states.pop();
		}
		if (states.isEmpty()) {
			states.push(Pair.of(UserState.DASHBOARD_PAGE, null));
			states.push(Pair.of(UserState.__BEFORE_NEXT_UPDATE, null));
		}
	}
	static void navigateToProperState(Long telegramId, Message message) {
		Stack<Pair<UserState, Integer>> eventualState = userState.get(telegramId);
		if (eventualState.isEmpty()) {
			if (Authentication.isSessionAuthenticated(telegramId)) {
				eventualState.push(Pair.of(UserState.DASHBOARD_PAGE, null));
			}
			else eventualState.push(Pair.of(UserState.AUTHENTICATION_NEEDED, null));
		} else if (!Authentication.isSessionAuthenticated(telegramId) &&
				!eventualState.peek().getLeft().isStateAuthRelated() &&
				!eventualState.peek().getLeft().isImmediateState()) {
			eventualState.push(Pair.of(UserState.AUTHENTICATION_NEEDED, null));
		}

		UserState properState = eventualState.peek().getLeft();

		System.out.println("the state now is: " + properState);

		var sentMsgId = switch (properState) {
			case AUTHENTICATION_NEEDED -> Authentication.authenticate(telegramId);
			case DASHBOARD_PAGE -> PageFactory.viewDashboardPage(telegramId, message.getMessageId());
//			case MANAGE_ORDERS_PAGE -> viewManageOrdersPage();
			case SETTINGS_PAGE -> PageFactory.viewSettingsPage(telegramId, message.getMessageId());
			case HELP_PAGE -> PageFactory.viewHelpPage(telegramId, message.getMessageId());
			default -> null;
		};

		if (sentMsgId != null) {
			updateKeyboardState(telegramId, sentMsgId, properState);
			if (eventualState.peek().getRight() != null) {
				MessageHandler.deleteMessage(telegramId, eventualState.pop().getRight(), TimeConstants.NO_DELAY_TIME.time());
				return;
			}
			eventualState.pop();
			return;
		}

		switch (properState) {
			case AUTHENTICATION_SIGNIN -> Authentication.signIn(message, telegramId);
			case AUTHENTICATION_SIGNUP -> Authentication.signUp(message, telegramId);
			case CANCEL_CURRENT_OPERATION_WITH_NOTICE -> cancelCurrentOperation(telegramId, false);
			case __CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE -> cancelCurrentOperation(telegramId, true);
			case SELECT_FAVORITE_RESTAURANT -> PageFactory.viewFavoriteRestaurants(telegramId);
//			case SEARCH_FOR_RESTAURANTS ->
//			case SEARCH_FOR_PRODUCTS ->
//			case CONFIRM_ORDER ->
//			case CANCEL_PENDING_ORDER -> cancelPendingOrder();
			case AUTHENTICATION_SIGNOUT -> Authentication.signOut(telegramId);
			case __PREVIOUS_KEYBOARD -> eventualState.push(Pair.of(keyboardState.get(telegramId).get(message.getMessageId())[0], null));
			default -> MessageFactory.sendIncorrectOperationNotice(telegramId);
		}

		if (eventualState.peek().getRight() != null) {
			MessageHandler.deleteMessage(telegramId, eventualState.pop().getRight(), TimeConstants.NO_DELAY_TIME.time());
		}
		if (properState == UserState.AUTHENTICATION_SIGNOUT) {
			for (Pair<UserState, Integer> userStateIntegerPair : eventualState) {
				MessageHandler.deleteMessage(telegramId, userStateIntegerPair.getRight(), TimeConstants.NO_DELAY_TIME.time());
			}
			eventualState.clear();
		}
	}
	private static void cancelCurrentOperation(Long telegramId, boolean isNoticeSent) {
		Stack<Pair<UserState, Integer>> states = userState.get(telegramId);
		assert (states.peek().getLeft() == UserState.CANCEL_CURRENT_OPERATION_WITH_NOTICE ||
				states.peek().getLeft() == UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE);
		Pair<UserState, Integer> cancelType = states.pop(); // so that NavigateToProperState can pop the current state if it was not an op.
		String message;
		if (states.empty() || !states.peek().getLeft().isOperationState()) {
			message = Emoji.ORANGE_CIRCLE.getCode() + " There is No Operation to Cancel\\, You are Good to Go " + Emoji.SMILING_FACE.getCode();
		} else {
			for (Pair<UserState, Integer> userStateIntegerPair : states) {
				MessageHandler.deleteMessage(telegramId, userStateIntegerPair.getRight(), TimeConstants.NO_DELAY_TIME.time());
			}
			states.clear();
			message = Emoji.BLUE_CIRCLE.getCode() + " Current Operation Has Been Canceled\\.";
		}
		if (!isNoticeSent) MessageHandler.sendShortNotice(telegramId, message);
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