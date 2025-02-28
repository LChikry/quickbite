package org.quickbitehub.state;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.authentication.AuthenticationService;
import org.quickbitehub.communicator.*;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.*;

public class State {
	private static AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());

	public static final Map<Long, Stack<Pair<UserState, Integer>>> userState = new HashMap<>(); // TelegramId -> State and messageId triggered the state to be deleted when finish
	public static final Map<Long, Pair<List<Integer>, UserState[]>> keyboardState = new HashMap<>(); // TelegramId -> keyboard MessageId[0] + relatedMsgs[1->inf] and KeyboardState[Previous, current];
	public static final int NUM_KEYBOARD_STATES = 2;


	public State(AuthenticationController authController) {
//		State.authController = authController;
	}

	public static void pushRequiredState(Long telegramId, Pair<UserState, Integer> newState) {
		var states = userState.get(telegramId);
		Pair<UserState, Integer> currentState = states.pop();
		states.push(newState);
		states.push(currentState);
	}
	public static void pushImmediateState(Long telegramId, Pair<UserState, Integer> recentState) {userState.get(telegramId).push(recentState);}
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
				MessageHandler.deleteMessage(telegramId, states.pop().getRight(), TimeConstants.NO_TIME.time());
			} else states.pop();
		}
		if (states.isEmpty()) {
			states.push(Pair.of(UserState.DASHBOARD_PAGE, null));
			states.push(Pair.of(UserState.__BEFORE_NEXT_UPDATE, null));
		}
	}
	public static void navigateToProperState(Long chatId, Message message) {
		Stack<Pair<UserState, Integer>> eventualState = userState.get(chatId);
		assert (eventualState.peek().getLeft() != null);
		if (eventualState.isEmpty() && authController.isChatAuthenticated(chatId)) {
			eventualState.push(Pair.of(UserState.DASHBOARD_PAGE, null));
		} else if (!authController.isChatAuthenticated(chatId) &&
				!eventualState.peek().getLeft().isStateAuthRelated() &&
				!eventualState.peek().getLeft().isImmediateState()) {
			clearUserState(chatId);
			eventualState.push(Pair.of(UserState.AUTHENTICATION_PAGE, null));
		}
		System.out.println(eventualState);
		Integer pageId = null, messageId = null, olderMessageId = null;
		String messageText = null;
		if (message != null) {
			messageId = message.getMessageId();
			if (message.getReplyToMessage() != null) olderMessageId = message.getReplyToMessage().getMessageId();
			if (message.getText() != null) messageText = message.getText();
		}

		UserState properState = eventualState.peek().getLeft();
		System.out.println("state: " + properState + "   msgId: " + messageId);
//		System.out.println("the state now is: " + properState);
		switch (properState) {
			case AUTHENTICATION_PAGE -> pageId = authController.viewAuthenticationPage(chatId, messageId);
			case SIGNIN_PAGE -> authController.viewSignInPage(chatId, messageId);
			case __SET_SIGNIN_EMAIL, __SET_SIGNIN_PASSWORD, __GET_SIGNIN_EMAIL, __GET_SIGNIN_PASSWORD, __CONFIRM_SIGNIN -> {
				authController.processSignIn(chatId, properState, messageId, olderMessageId, messageText);
			}
			case SIGNUP_PAGE -> authController.viewSignUpPage(chatId, messageId);
			case __SET_SIGNUP_EMAIL, __SET_SIGNUP_PASSWORD, __SET_SIGNUP_FIRST_NAME, __SET_SIGNUP_LAST_NAME,
			     __SET_SIGNUP_MIDDLE_NAMES, __GET_SIGNUP_EMAIL, __GET_SIGNUP_PASSWORD, __GET_SIGNUP_FIRST_NAME,
			     __GET_SIGNUP_MIDDLE_NAMES, __GET_SIGNUP_LAST_NAME, __CONFIRM_SIGNUP -> {
				authController.processSignUp(chatId, properState, messageId, olderMessageId, messageText);
			}
			case DASHBOARD_PAGE -> pageId = PageFactory.viewDashboardPage(chatId, messageId);
//			case MANAGE_ORDERS_PAGE -> pageId = viewManageOrdersPage();
			case SELECT_FAVORITE_RESTAURANT -> PageFactory.viewFavoriteRestaurants(chatId);
//			case SEARCH_FOR_RESTAURANTS ->
//			case SEARCH_FOR_PRODUCTS ->
//			case CONFIRM_ORDER ->
//			case CANCEL_PENDING_ORDER -> cancelPendingOrder();
			case SIGNOUT -> {
				authController.signOut(chatId);
				clearUserState(chatId);
			}
			case SETTINGS_PAGE -> pageId = PageFactory.viewSettingsPage(chatId, messageId);
			case HELP_PAGE -> pageId = PageFactory.viewHelpPage(chatId, messageId);
			case __PREVIOUS_KEYBOARD -> goBack(chatId, keyboardState.get(chatId).getRight()[0], messageId);
			case CANCEL_CURRENT_OPERATION_WITH_NOTICE -> cancelCurrentOperation(chatId, false);
			case __CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE -> cancelCurrentOperation(chatId, true);
			default -> MessageFactory.sendIncorrectOperationNotice(chatId);
		}
		if (properState.isPageCreationState()) updateKeyboardState(chatId, pageId, properState);
		if (properState != UserState.SIGNOUT) {
			MessageHandler.deleteMessage(chatId, eventualState.pop().getRight(), TimeConstants.NO_TIME.time());
		}
	}
	private static void clearUserState(Long chatId) {
		var state = userState.get(chatId);
		for (Pair<UserState, Integer> userStateIntegerPair : state) {
			MessageHandler.deleteMessage(chatId, userStateIntegerPair.getRight(), TimeConstants.NO_TIME.time());
		}
		state.clear();
		clearKeyboardState(chatId);
	}
	private static void clearKeyboardState(Long chatId) {
		var keyboard = keyboardState.get(chatId);
		var messages = keyboard.getLeft();
		Integer prevKeyboardId = null;
		if (!messages.isEmpty()) prevKeyboardId= messages.getFirst();
		for (int i = 0; i < messages.size(); ++i) {
			MessageHandler.deleteMessage(chatId, messages.get(i), TimeConstants.NO_TIME.time());
		}
		messages.clear();

	 // TelegramId -> keyboard MessageId[0] + relatedMsgs[1->inf] and KeyboardState[Previous, current];
	}
	private static void goBack(Long telegramId, UserState properState, Integer messageId) {
		Integer pageId = null;
		switch (properState) {
			case AUTHENTICATION_PAGE -> pageId = authController.viewAuthenticationPage(telegramId, messageId);
			case SIGNIN_PAGE -> authController.viewSignInPage(telegramId, messageId);
			case SIGNUP_PAGE -> authController.viewSignUpPage(telegramId, messageId);
			case DASHBOARD_PAGE -> pageId = PageFactory.viewDashboardPage(telegramId, messageId);
//			case MANAGE_ORDERS_PAGE -> pageId = viewManageOrdersPage();
			case SETTINGS_PAGE -> pageId = PageFactory.viewSettingsPage(telegramId, messageId);
			case HELP_PAGE -> pageId = PageFactory.viewHelpPage(telegramId, messageId);
		}
		updateKeyboardState(telegramId, pageId, properState);
	}
	private static void cancelCurrentOperation(Long telegramId, boolean isNoticeSent) {
		Stack<Pair<UserState, Integer>> states = userState.get(telegramId);
		assert (states.peek().getLeft() == UserState.CANCEL_CURRENT_OPERATION_WITH_NOTICE ||
				states.peek().getLeft() == UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE);
		Pair<UserState, Integer> cancelType = states.pop(); // so that NavigateToProperState can pop the current state if it was not an op.
		String message;
		if (states.empty() || !states.peek().getLeft().isOperationState()) {
			message = Emoji.ORANGE_CIRCLE.getCode() + " There is No Operation to Cancel, You are Good to Go " + Emoji.SMILING_FACE.getCode();
		} else {
			for (Pair<UserState, Integer> userStateIntegerPair : states) {
				MessageHandler.deleteMessage(telegramId, userStateIntegerPair.getRight(), TimeConstants.NO_TIME.time());
			}
			states.clear();
			message = Emoji.BLUE_CIRCLE.getCode() + " Current Operation Has Been Canceled.";
		}
		if (!isNoticeSent) MessageHandler.sendShortNotice(telegramId, message);
	}
	/*
		Deletes all keyboards and left only one keyboard in the chat
		Keep track of that keyboard state (right) and id (left[0])
		Keep track of all msgs (their id) that is related to that keyboard ([1] -> infinity)
	 */
	public static void updateKeyboardState(Long telegramId, Integer nextKeyboardId, UserState nextKeyboard) {
		var keyboard = keyboardState.get(telegramId);
		var messages = keyboard.getLeft();
		Integer prevKeyboardId = null;
		if (!messages.isEmpty()) prevKeyboardId= messages.getFirst();
		for (int i = 1; i < messages.size(); ++i) {
			MessageHandler.deleteMessage(telegramId, messages.get(i), TimeConstants.NO_TIME.time());
		}
		messages.clear();

		if (nextKeyboardId == null) { // edit keyboard
			assert (prevKeyboardId != null);
			messages.addFirst(prevKeyboardId);
			keyboard.getRight()[0] = keyboard.getRight()[1];
		} else { // new keyboard
			if (prevKeyboardId!= null) MessageHandler.deleteMessage(telegramId, prevKeyboardId, TimeConstants.NO_TIME.time());
			messages.addFirst(nextKeyboardId);
			switch (nextKeyboard) {
				case AUTHENTICATION_PAGE, DASHBOARD_PAGE -> keyboard.getRight()[0] = null;
				default -> {
					if (nextKeyboard == keyboard.getRight()[1]) break; // to make sure prev & current !same
					keyboard.getRight()[0] = keyboard.getRight()[1];
				}
			}
		}
		keyboard.getRight()[1] = nextKeyboard;
	}
	public static void addKeyboardRelatedMessage(Long telegramId, Integer messageId) {
		keyboardState.get(telegramId).getLeft().add(messageId);
	}
}