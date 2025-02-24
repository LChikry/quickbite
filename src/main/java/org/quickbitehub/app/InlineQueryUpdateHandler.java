package org.quickbitehub.app;

import org.telegram.telegrambots.meta.api.objects.Update;

class InlineQueryUpdateHandler extends UpdateHandler {
	public void handleUpdate(Update update, Long chatId) {
		if (nextHandler != null) nextHandler.handleUpdate(update, chatId);
	}
}