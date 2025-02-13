package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.communicator.MessageFactory;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.quickbitehub.state.State.navigateToProperState;

public class ReplyUpdateHandler extends UpdateHandler {
	private final AuthenticationController authController;

	public ReplyUpdateHandler(AuthenticationController authController) {
		this.authController = authController;
	}

	public void handleUpdate(Update update, Long chatId) {
		if (!update.hasMessage() || !update.getMessage().isReply()) {
			if (nextHandler != null) nextHandler.handleUpdate(update, chatId);
			return;
		}
		String messageId = String.valueOf(update.getMessage().getReplyToMessage().getMessageId());

		if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNIN_EMAIL))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNIN_EMAIL, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNIN_PASSWORD))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNIN_PASSWORD, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNUP_EMAIL))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNUP_EMAIL, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNUP_PASSWORD))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNUP_PASSWORD, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNUP_FIRST_NAME))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNUP_FIRST_NAME, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNUP_LAST_NAME))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNUP_LAST_NAME, null));
		} else if (messageId.equals(authController.getAuthStateValue(chatId, UserState.__SET_SIGNUP_MIDDLE_NAMES))) {
			State.pushImmediateState(chatId, Pair.of(UserState.__GET_SIGNUP_MIDDLE_NAMES, null));
		} else {
			MessageFactory.sendIncorrectInputNotice(chatId);
			return;
		}

		navigateToProperState(chatId, update.getMessage());
	}
}
