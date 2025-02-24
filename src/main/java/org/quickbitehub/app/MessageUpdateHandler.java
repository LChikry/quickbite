package org.quickbitehub.app;

import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.provider.Restaurant;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;

class MessageUpdateHandler extends UpdateHandler {
	public void handleUpdate(Update update, Long chatId) {
		if (!update.hasMessage() || !update.getMessage().hasText()) {
			if (nextHandler != null) nextHandler.handleUpdate(update, chatId);
			return;
		}

		Restaurant restaurant = Restaurant.allRestaurants.get(update.getMessage().getText());
		if (restaurant == null ||
				State.isUserStateless(chatId) ||
				State.getUserState(chatId) != UserState.SELECT_FAVORITE_RESTAURANT) {
			if (update.getMessage().getText().contains("search for restaurants")) {
				// task: delete that message of searching + push the state of searching for a restaurant
			}
			return;
		}
		System.out.println("we detected a restaurant");
		// task: show the available products and ask for the quantity for each product

		MessageHandler.deleteMessage(chatId, update.getMessage().getMessageId(), TimeConstants.NO_TIME.time());
	}
}