package org.quickbitehub.app;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Stack;

import static org.quickbitehub.state.State.*;

class StateUpdateHandler extends UpdateHandler {
	public void handleUpdate(Update update, Long chatId) {
		State.userState.putIfAbsent(chatId, new Stack<>());
		State.keyboardState.putIfAbsent(chatId, Pair.of(new ArrayList<>(), new UserState[NUM_KEYBOARD_STATES]));

		if (nextHandler != null) nextHandler.handleUpdate(update, chatId);

		while (!State.isUserStateless(chatId) && State.getUserState(chatId) == UserState.__BEFORE_NEXT_UPDATE) {
			userState.get(chatId).pop();
			navigateToProperState(chatId, null);
		}
	}
}