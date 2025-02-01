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
	private static final String TEXT_PARSE_MODE = "MarkdownV2";

	private static String addMarkdownMarkUp(String message) {
		String specialChars = "[]()~`>#.+'-=|{}!";
		StringBuilder escapedString = new StringBuilder();

		for (char ch : message.toCharArray()) {
			if (specialChars.indexOf(ch) != -1) {
				escapedString.append("\\");
			}
			escapedString.append(ch);
		}
		return escapedString.toString();
	}

	public static void shutdownScheduler() {
		Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
	}
	public static Integer sendForceReply(Long telegramId, String message, long autoDeleteDelayTime) {
		message = addMarkdownMarkUp(message);
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode(TEXT_PARSE_MODE)
				.text(message)
				.replyMarkup(KeyboardFactory.getForceReplyKeyboard())
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime != TimeConstants.NO_TIME.time()) {
				scheduler.schedule(() -> deleteMessage(telegramId, sentMessage.getMessageId()),
						TimeConstants.STANDARD_DELAY_TIME_SEC.time(),
						TimeUnit.SECONDS);
			}
			return sentMessage.getMessageId();
		} catch (TelegramApiException e) {
			System.out.println("MessageHandler: sendInlineKeyboard");
			e.printStackTrace();
		}
		return null;
	}
	public static Message sendInlineKeyboard(Long telegramId, String message, InlineKeyboardMarkup kb, long autoDeleteDelayTime){
		message = addMarkdownMarkUp(message);
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode(TEXT_PARSE_MODE)
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime != TimeConstants.NO_TIME.time()) {
				scheduler.schedule(() -> deleteMessage(telegramId, sentMessage.getMessageId()),
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
		message = addMarkdownMarkUp(message);
		SendMessage sm = SendMessage
				.builder()
				.chatId(telegramId)
				.parseMode(TEXT_PARSE_MODE)
				.text(message)
				.replyMarkup(kb)
				.build();

		try {
			Message sentMessage = telegramClient.execute(sm);
			if (autoDeleteDelayTime != TimeConstants.NO_TIME.time()) {
				scheduler.schedule(() -> deleteMessage(telegramId, sentMessage.getMessageId()),
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
	private static void deleteMessage(Long telegramId, Integer messageId) {
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
		if (messageId == null) return;
		if (autoDeleteDelayTime == TimeConstants.NO_TIME.time()) {
			deleteMessage(telegramId, messageId);
			return;
		}
		scheduler.schedule(() -> deleteMessage(telegramId, messageId),
				autoDeleteDelayTime,
				TimeUnit.SECONDS);
	}
	public static void editInlineKeyboardOnly(Long telegramId, Integer messageId, InlineKeyboardMarkup kb) {
		EditMessageReplyMarkup newKeyboard = EditMessageReplyMarkup
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.replyMarkup(kb)
				.build();
		try {
			telegramClient.execute(newKeyboard);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	public static void editInlineKeyboardAndMessage(Long telegramId, Integer messageId, String message, InlineKeyboardMarkup kb) {
		message = addMarkdownMarkUp(message);
		EditMessageText newContent = EditMessageText
				.builder()
				.chatId(telegramId)
				.messageId(messageId)
				.text(message)
				.parseMode(TEXT_PARSE_MODE)
				.build();
		try {
			telegramClient.execute(newContent);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		editInlineKeyboardOnly(telegramId, messageId, kb);
	}
	public static void sendText(Long telegramId, String textMessage, long autoDeleteDelayTime) {
		textMessage = addMarkdownMarkUp(textMessage);
		SendMessage msg = SendMessage
				.builder()
				.chatId(telegramId)
				.text(textMessage)
				.parseMode(TEXT_PARSE_MODE)
				.build();

		try {
			Message sentMessage = telegramClient.execute(msg);
			if (autoDeleteDelayTime != TimeConstants.NO_TIME.time()) {
				scheduler.schedule(() -> deleteMessage(telegramId, sentMessage.getMessageId()),
						autoDeleteDelayTime,
						TimeUnit.SECONDS);
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
			System.out.println("MessageHandler: sendText");
		}
	}
	public static void sendShortNotice(long telegramId, String noticeMessage) {
		sendText(telegramId, noticeMessage, TimeConstants.SHORT_DELAY_TIME_SEC.time());
	}
	public static void sendLongNotice(long telegramId, String noticeMessage) {
		sendText(telegramId, noticeMessage, TimeConstants.STANDARD_DELAY_TIME_SEC.time());
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