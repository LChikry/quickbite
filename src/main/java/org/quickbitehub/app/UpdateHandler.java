package org.quickbitehub.app;

import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class UpdateHandler {
	protected UpdateHandler nextHandler;

	public static UpdateHandler chain(UpdateHandler firstHandler, UpdateHandler... handlers) {
		UpdateHandler current = firstHandler;
		for (UpdateHandler next: handlers) {
			current.nextHandler = next;
			current = next;
		}
		return firstHandler;
	}

	public abstract void handleUpdate(Update update, Long chatId);
}