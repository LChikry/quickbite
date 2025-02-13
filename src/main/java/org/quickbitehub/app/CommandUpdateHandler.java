package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.quickbitehub.state.State.navigateToProperState;

public class CommandUpdateHandler extends UpdateHandler {
	private final AuthenticationController authController;
	public CommandUpdateHandler(AuthenticationController authController) {
		this.authController = authController;
	}

	public void handleUpdate(Update update, Long chatId) {
		if (!update.hasMessage() || !update.getMessage().isCommand()) {
			if (nextHandler != null) nextHandler.handleUpdate(update, chatId);
			return;
		}

		Message message = update.getMessage();
		UserState newState = UserState.getValueOf(message.getText());
		if (State.isUserStateless(chatId) ||
				authController.isChatAuthenticated(chatId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated() ||
				!State.getUserState(chatId).isStateAuthRelated()) {
			State.pushImmediateState(chatId, Pair.of(newState, message.getMessageId()));
		}
		navigateToProperState(chatId, null);
	}
}
