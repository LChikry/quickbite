package org.quickbitehub.app;

import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.communicator.PageFactory;
import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.communicator.MessageHandler;

import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.Instant;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	public static final String BOT_USERNAME = "QuickBiteHub_bot";
	private final AuthenticationController authController;
	private final UpdateHandler updateHandler;

	public QuickBite(AuthenticationController authController) {
		this.authController = authController;
		PageFactory.authController = authController;

		updateHandler = UpdateHandler.chain(
				new StateUpdateHandler(),
				new CommandUpdateHandler(authController),
				new CallBackQueryUpdateHandler(authController),
				new ReplyUpdateHandler(authController),
				new MessageUpdateHandler(),
				new InlineQueryUpdateHandler()
		);
	}

	private Long extractChatId(Update update) {
		assert (update != null);
		if (update.hasMessage()) return update.getMessage().getChatId();
		if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom().getId();
		if (update.hasInlineQuery()) return update.getInlineQuery().getFrom().getId();
		return null;
	}
	private boolean isMessageExpired(Update update) {
		if (!update.hasMessage()) return false;
		int UPDATE_TIME_OUT_DURATION_SEC = 20;
		if (update.getMessage().getDate() + UPDATE_TIME_OUT_DURATION_SEC >= Instant.now().getEpochSecond()) return false;
		MessageHandler.deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), TimeConstants.NO_TIME.time());
		return true;
	}

	@Override
	public void consume(Update update) {
		if (isMessageExpired(update)) return;
		updateHandler.handleUpdate(update, extractChatId(update));
	}
}