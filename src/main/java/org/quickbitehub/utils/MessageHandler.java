package org.quickbitehub.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.QuickBite;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.TimeUnit;

public class MessageHandler {
	private static final TelegramClient telegramClient = new OkHttpTelegramClient(Dotenv.load().get("BOT_TOKEN"));

	public static Message sendForceReply(Long telegramId, String message, long autoDeleteDelayTime) {
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(KeyboardFactory.getForceReplyKeyboard())
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime > 0) {
				QuickBite.scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
						autoDeleteDelayTime,
						TimeUnit.SECONDS);
			}
			return sentMessage;
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendInlineKeyboard");
			e.printStackTrace();
		}
		return null;
	}

	public static Message sendInlineKeyboard(Long telegramId, String message, InlineKeyboardMarkup kb, long autoDeleteDelayTime){
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime > 0) {
				QuickBite.scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
						autoDeleteDelayTime,
						TimeUnit.SECONDS);
			}
			return sentMessage;
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendInlineKeyboard");
			e.printStackTrace();
		}
		return null;
	}

	public static Message sendReplyKeyboard(Long telegramId, String message, ReplyKeyboardMarkup kb, long autoDeleteDelayTime) {
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime > 0) {
				QuickBite.scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
						autoDeleteDelayTime,
						TimeUnit.SECONDS);
			}
			return sentMessage;
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendReplyKeyboard");
			e.printStackTrace();
		}
		return null;
	}

	public static void deleteMessage(Long telegramId, Integer messageId) {
		DeleteMessage dm = DeleteMessage
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.build();

		try {
			telegramClient.execute(dm);
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: deleteMessage()");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static Message sendText(Long telegramId, String textMessage, long autoDeleteDelayTime) {
		SendMessage msg = SendMessage
				.builder()
				.chatId(telegramId)
				.text(textMessage)
				.parseMode("MarkdownV2")
				.build();

		try {
			Message sentMessage = telegramClient.execute(msg);
			if (autoDeleteDelayTime > 0) {
				QuickBite.scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
						autoDeleteDelayTime,
						TimeUnit.SECONDS);
			}
			return sentMessage;
		} catch (TelegramApiException e) {
			e.printStackTrace();
			System.out.println("MessageHandler: sendText");
		}
		return null;
	}
}
