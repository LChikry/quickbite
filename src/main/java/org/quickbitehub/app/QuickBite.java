package org.quickbitehub.app;

import org.quickbitehub.authentication.Authentication;

import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.provider.Restaurant;
import org.quickbitehub.communicator.MessageHandler;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Instant;
import java.util.HashMap;
import java.util.Stack;

import static org.quickbitehub.app.State.*;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	public static final String BOT_USERNAME = "QuickBiteHub_bot";

	@Override
	public void consume(Update update) {
		long telegramId = -1;
		if (update.hasMessage()) {
			Message msg = update.getMessage();
			telegramId = msg.getChatId();
			if (msg.getDate() + 20 < Instant.now().getEpochSecond()) {
				MessageHandler.deleteMessage(telegramId, msg.getMessageId());
				return;
			}
			if (State.isUserStateless(telegramId)) userState.put(telegramId, new Stack<>());
			if (!keyboardState.containsKey(telegramId)) keyboardState.put(telegramId, new HashMap<>());
			if (msg.isCommand()) botCommandsHandler(msg);
			else if (msg.isReply()) botRepliesHandler(msg);
			else if (msg.hasText()) botMessageHandler(msg);
		} else if (update.hasCallbackQuery()) {
			telegramId = update.getCallbackQuery().getFrom().getId();
			if (State.isUserStateless(telegramId)) userState.put(telegramId, new Stack<>());
			if (!keyboardState.containsKey(telegramId)) keyboardState.put(telegramId, new HashMap<>());
			botCallBackQueryHandler(update.getCallbackQuery());
		} else if (update.hasInlineQuery()) botInlineQueryHandler(update.getInlineQuery());

		assert (userState.get(telegramId) != null && keyboardState.get(telegramId) != null);
		if (telegramId != -1 && !State.isUserStateless(telegramId) && State.getUserState(telegramId) == UserState.__BEFORE_NEXT_UPDATE) {
			userState.get(telegramId).pop();
			navigateToProperState(telegramId, null);
		}
	}
	private void botCommandsHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		UserState newState = UserState.getValueOf(message.getText());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), SHORT_DELAY_TIME_SEC);
		if (State.isUserStateless(telegramId) ||
				Authentication.isSessionAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated() ||
				!State.getUserState(telegramId).isStateAuthRelated()) {
			State.pushImmediateState(telegramId, newState);
		}
		navigateToProperState(telegramId, null);
	}
	private void botCallBackQueryHandler(CallbackQuery cbq) {
		Long telegramId = cbq.getFrom().getId();
		UserState newState = UserState.getValueOf(cbq.getData());
		if (State.isUserStateless(telegramId) ||
				Authentication.isSessionAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated()) {
			State.pushImmediateState(telegramId, newState);
		}
		navigateToProperState(telegramId, cbq.getMessage().getMessageId());
		MessageHandler.answerCallBackQuery(cbq.getId());
	}
	private void botRepliesHandler(Message message) {
		Long telegramId = message.getChat().getId();
		UserState currentState = State.getUserState(telegramId);

		System.out.println("we in reply, state is: " + currentState);
		switch (currentState) {
			case AUTHENTICATION_SIGNIN -> Authentication.signIn(message, telegramId);
			case AUTHENTICATION_SIGNUP -> Authentication.signUp(message, telegramId);
		}
		// deleting the message then the reply
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.NO_DELAY_TIME.time());
	}
	private void botMessageHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		Restaurant restaurant = Restaurant.allRestaurants.get(message.getText());
		if (restaurant == null ||
				State.isUserStateless(telegramId) ||
				State.getUserState(telegramId) != UserState.SELECT_FAVORITE_RESTAURANT) {
			return;
		}
		System.out.println("we detected a restaurant");
		// task: show the available products and ask for the quantity for each product

		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.SHORT_DELAY_TIME_SEC.time());
	}
	private void botInlineQueryHandler(InlineQuery inlineQuery) {
	}
}