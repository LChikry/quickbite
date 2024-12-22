package org.quickbitehub;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageHandler {
	private TelegramClient telegramClient;
	private long interval;

	public MessageHandler(TelegramClient client) {
		telegramClient = client;
		interval = 0L;
	}

	public boolean textMessage(long user, String textMessage) {
		SendMessage msg = SendMessage
				.builder()
				.chatId(user)
				.text(textMessage)
				.parseMode("MarkdownV2")
				.build();

		try {
			telegramClient.execute(msg);
			return true;
		} catch (TelegramApiException e) {
			interval = (long) Math.pow(2, interval);
			System.out.println(interval);
			return textMessageRegression(user, textMessage);
		}
	}

	private boolean textMessageRegression(long user, String textMessage) {
		if (interval >= 4) return false; // try sending text 2 more times

		try {
			Thread.sleep(1000 * interval);
		} catch (InterruptedException ex) {
		}

		if (textMessage(user, textMessage)) {
			interval = 0L;
			return true;
		}
		interval = 0L;
		return false;
	}
}
