package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.quickbitehub.state.State.navigateToProperState;

class CallBackQueryUpdateHandler extends UpdateHandler {
	private final AuthenticationController authController;

	public CallBackQueryUpdateHandler(AuthenticationController authController) {
		this.authController = authController;
	}

	public void handleUpdate(Update update, Long chatId) {
		if (!update.hasCallbackQuery()) {
			if (nextHandler != null) nextHandler.handleUpdate(update, chatId);
			return;
		}

		UserState newState = UserState.getValueOf(update.getCallbackQuery().getData());
		if (State.isUserStateless(chatId) ||
				authController.isChatAuthenticated(chatId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated()) {
			State.pushImmediateState(chatId, Pair.of(newState, null));
		}
		navigateToProperState(chatId, (Message) update.getCallbackQuery().getMessage());
		MessageHandler.answerCallBackQuery(update.getCallbackQuery().getId());
	}
}