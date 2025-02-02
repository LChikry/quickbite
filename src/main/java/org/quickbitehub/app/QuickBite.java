package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.AuthenticationController;

import org.quickbitehub.communicator.MessageFactory;
import org.quickbitehub.communicator.PageFactory;
import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.provider.Restaurant;
import org.quickbitehub.communicator.MessageHandler;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Stack;

import static org.quickbitehub.app.State.*;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	public static final String BOT_USERNAME = "QuickBiteHub_bot";
	private final AuthenticationController authController;
	
	public QuickBite(AuthenticationController authController) {
		this.authController = authController;
		PageFactory.authController = authController;
	}

	private Long extractTelegramId(Update update) {
		if (update.hasMessage()) return update.getMessage().getChatId();
		if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom().getId();
		if (update.hasInlineQuery()) return update.getInlineQuery().getFrom().getId();
		return null;
	}
	private boolean isMessageExpired(Update update) {
		if (!update.hasMessage()) return false;
		if (update.getMessage().getDate() + 20 >= Instant.now().getEpochSecond()) return false;
		MessageHandler.deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), TimeConstants.NO_TIME.time());
		return true;
	}
	@Override
	public void consume(Update update) {
		if (isMessageExpired(update)) return;
		Long telegramId = extractTelegramId(update);
		if (telegramId == null) return;
		userState.putIfAbsent(telegramId, new Stack<>());
		keyboardState.putIfAbsent(telegramId, Pair.of(new ArrayList<>(), new UserState[NUM_KEYBOARD_STATES]));

		if (update.hasMessage()) {
			Message msg = update.getMessage();
			if (msg.isCommand()) botCommandsHandler(msg);
			else if (msg.isReply()) botRepliesHandler(msg);
			else if (msg.hasText()) botMessageHandler(msg);
		} else if (update.hasCallbackQuery()) botCallBackQueryHandler(update.getCallbackQuery());
		else if (update.hasInlineQuery()) botInlineQueryHandler(update.getInlineQuery());

		if (!State.isUserStateless(telegramId) && State.getUserState(telegramId) == UserState.__BEFORE_NEXT_UPDATE) {
			userState.get(telegramId).pop();
			navigateToProperState(telegramId, null);
		}
	}
	private void botCommandsHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		UserState newState = UserState.getValueOf(message.getText());
		if (State.isUserStateless(telegramId) ||
				authController.isChatAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated() ||
				!State.getUserState(telegramId).isStateAuthRelated()) {
			State.pushImmediateState(telegramId, Pair.of(newState, message.getMessageId()));
		}
		navigateToProperState(telegramId, null);
	}
	private void botCallBackQueryHandler(CallbackQuery cbq) {
		Long telegramId = cbq.getFrom().getId();
		UserState newState = UserState.getValueOf(cbq.getData());
		if (State.isUserStateless(telegramId) ||
				authController.isChatAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated()) {
			State.pushImmediateState(telegramId, Pair.of(newState, null));
		}
		navigateToProperState(telegramId, (Message) cbq.getMessage());
		MessageHandler.answerCallBackQuery(cbq.getId());
	}
	private void botRepliesHandler(Message message) {
		Long telegramId = message.getChat().getId();
		String messageId = String.valueOf(message.getReplyToMessage().getMessageId());

		if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNIN_EMAIL))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNIN_EMAIL, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNIN_PASSWORD))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNIN_PASSWORD, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNUP_EMAIL))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNUP_EMAIL, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNUP_PASSWORD))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNUP_PASSWORD, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNUP_FIRST_NAME))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNUP_FIRST_NAME, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNUP_LAST_NAME))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNUP_LAST_NAME, null));
		} else if (messageId.equals(authController.getAuthStateValue(telegramId, UserState.__SET_SIGNUP_MIDDLE_NAMES))) {
			State.pushImmediateState(telegramId, Pair.of(UserState.__GET_SIGNUP_MIDDLE_NAMES, null));
		} else {
			MessageFactory.sendIncorrectInputNotice(telegramId);
			return;
		}

		navigateToProperState(telegramId, message);
	}
	private void botMessageHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		Restaurant restaurant = Restaurant.allRestaurants.get(message.getText());
		if (restaurant == null ||
				State.isUserStateless(telegramId) ||
				State.getUserState(telegramId) != UserState.SELECT_FAVORITE_RESTAURANT) {
			if (message.getText().contains("search for restaurants")) {
				// task: delete that message of searching + push the state of searching for a restaurant
			}
			return;
		}
		System.out.println("we detected a restaurant");
		// task: show the available products and ask for the quantity for each product

		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.NO_TIME.time());
	}
	private void botInlineQueryHandler(InlineQuery inlineQuery) {
	}
}