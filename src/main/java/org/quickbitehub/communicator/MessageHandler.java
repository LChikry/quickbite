package org.quickbitehub.communicator;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageHandler {
	private static final TelegramClient telegramClient = new OkHttpTelegramClient(Dotenv.load().get("BOT_TOKEN"));
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void shutdownScheduler() {
		Runtime.getRuntime().addShutdownHook(new Thread(MessageHandler.scheduler::shutdown));
	}

	public static Message sendForceReply(Long telegramId, String message) {
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode("MarkdownV2")
				.text(message)
				.replyMarkup(KeyboardFactory.getForceReplyKeyboard())
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
						PageFactory.STANDARD_DELAY_TIME_SEC,
						TimeUnit.SECONDS);
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
				scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
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
				scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
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
			if (e.getMessage().contains("[400]")) return;
			System.out.println("MessageHandler: deleteMessage()");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void deleteMessage(Long telegramId, Integer messageId, long autoDeleteDelayTime) {
		if (autoDeleteDelayTime <= 0) {
			deleteMessage(telegramId, messageId);
			return;
		}
		scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, messageId),
				autoDeleteDelayTime,
				TimeUnit.SECONDS);
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
				scheduler.schedule(() -> MessageHandler.deleteMessage(telegramId, sentMessage.getMessageId()),
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

	public static void editInlineKeyboard(Long telegramId, Integer messageId, String message, InlineKeyboardMarkup kb) {
		EditMessageText newContent = EditMessageText
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.text(message)
				.parseMode("MarkdownV2")
				.build();

		EditMessageReplyMarkup newKeyboard = EditMessageReplyMarkup
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.replyMarkup(kb)
				.build();
		try {
			telegramClient.execute(newContent);
			telegramClient.execute(newKeyboard);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public static void answerCallBackQuery(String cbqId) {
		AnswerCallbackQuery close = AnswerCallbackQuery
				.builder()
				.callbackQueryId(cbqId)
				.build();
		try {
			telegramClient.execute(close);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
