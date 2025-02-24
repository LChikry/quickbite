package org.quickbitehub.app;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

abstract class UpdateHandler {
	protected UpdateHandler nextHandler;

	/**
	 * @param firstHandler mandatory first handler since the chain should have at least one handler
	 * @param handlers optional list of handlers that will make the chain in same order they are passed.
	 *                 If MessageUpdateHandler was passed before Command/Reply handlers, it will get rearranged
	 *                 to be after both of them.
	 * @return StateUpdateHandler as the first handler in the chain even if you didn't passed it
	 */
	public static UpdateHandler chain(UpdateHandler firstHandler, UpdateHandler... handlers) {
		// Ensuring uniqueness
		Set<Class<? extends UpdateHandler>> handlerTypes = new HashSet<>();
		List<UpdateHandler> handlerList = new ArrayList<>();
		handlerTypes.add(firstHandler.getClass());
		handlerList.add(firstHandler);
		for (UpdateHandler handler : handlers) {
			Class<? extends UpdateHandler> handlerType = handler.getClass();
			if (handlerTypes.add(handlerType)) handlerList.add(handler);
		}

		// ALWAYS StateUpdateHandler is the first handler
		UpdateHandler current = handlerList.removeFirst();
		if (!(firstHandler instanceof StateUpdateHandler)) {
			firstHandler = new StateUpdateHandler();
			firstHandler.nextHandler = current;
		}

		// ALWAYS keep MessageUpdateHandler after Command/Reply handler
		int messageIndex = -1, lastCommandOrReplyIndex = -1;
		for (int i = 0; i < handlerList.size(); i++) {
			if (handlerList.get(i) instanceof MessageUpdateHandler) messageIndex = i;
			else if (handlerList.get(i) instanceof CommandUpdateHandler ||
					handlerList.get(i) instanceof ReplyUpdateHandler) {
				lastCommandOrReplyIndex = i;
			}
		}
		if (messageIndex != -1 && messageIndex < lastCommandOrReplyIndex) {
			UpdateHandler messageHandler = handlerList.remove(messageIndex);
			handlerList.add(lastCommandOrReplyIndex + 1, messageHandler);
		}

		// Chaining the handlers
		for (UpdateHandler next: handlerList) {
			current.nextHandler = next;
			current = next;
		}
		return firstHandler;
	}

	public abstract void handleUpdate(Update update, Long chatId);
}