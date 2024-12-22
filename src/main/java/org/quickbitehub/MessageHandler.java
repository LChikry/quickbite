package org.quickbitehub;

import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


import javax.swing.plaf.TableHeaderUI;
import java.util.HashMap;

public class MessageHandler {
	private TelegramClient telegramClient;
	private long interval;
	public static HashMap<KeyboardType, ReplyKeyboard> keyboards = KeyboardFactory.getKeyboardList();

	public MessageHandler(TelegramClient client) {
		telegramClient = client;
		interval = 0L;
	}

	public String logInMenu(Long telegramId) {

		// show a login or signup menu
		// based on the choice show the corresponding menu

		// show the log in keyboard
		//switch_inline_query_current_chat
		return "";
	}

	public Message sendForceReply(Long telegramId, String message){
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup((ForceReplyKeyboard) keyboards.get(KeyboardType.FORCE_REPLY))
				.build();

		try {
			return telegramClient.execute(sm);
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendButtonKeyboard");
			e.printStackTrace();
		}
		return null;
	}

	public void sendButtonKeyboard(Long telegramId, String message, InlineKeyboardMarkup kb){
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			telegramClient.execute(sm);
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendButtonKeyboard");
			e.printStackTrace();
		}
	}

	public void deleteMessage(Long telegramId, Integer messageId) {
		DeleteMessage dm = DeleteMessage
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.build();

		try {
			telegramClient.execute(dm);
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: deleteMessage()");
			e.printStackTrace();
		}
	}

	public boolean sendText(Long user, String textMessage) {
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
			e.printStackTrace();
			System.out.println("MessageHandler: sendText");
			interval = (long) Math.pow(2, interval);
			return textMessageRegression(user, textMessage);
		}
	}

	private boolean textMessageRegression(long user, String textMessage) {
		if (interval >= 4) return false; // try sending text 2 more times

		try {
			Thread.sleep(1000 * interval);
		} catch (InterruptedException e) {
			System.out.println("MessageHandler: textMessageRegression()");
			e.printStackTrace();
		}

		if (sendText(user, textMessage)) {
			interval = 0L;
			return true;
		}
		interval = 0L;
		return false;
	}
}
