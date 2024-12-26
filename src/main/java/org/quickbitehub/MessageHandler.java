package org.quickbitehub;

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
	public static HashMap<KeyboardType, ReplyKeyboard> keyboards = KeyboardFactory.getKeyboardList();

	public MessageHandler(TelegramClient client) {
		telegramClient = client;
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

	public Message sendButtonKeyboard(Long telegramId, String message, InlineKeyboardMarkup kb){
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			return telegramClient.execute(sm);
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendButtonKeyboard");
			e.printStackTrace();
		}
		return null;
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

	public Message sendText(Long user, String textMessage) {
		SendMessage msg = SendMessage
				.builder()
				.chatId(user)
				.text(textMessage)
				.parseMode("MarkdownV2")
				.build();

		try {
			return telegramClient.execute(msg);
		} catch (TelegramApiException e) {
			e.printStackTrace();
			System.out.println("MessageHandler: sendText");
		}
		return null;
	}

}
