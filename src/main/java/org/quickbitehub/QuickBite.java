package org.quickbitehub;

import org.quickbitehub.client.Account;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Stack;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	private TelegramClient telegramClient;
	private HashMap<Long, Account> usersAccounts;
	private HashMap<Long, String> userSessions;
	private HashMap<Long, Stack<NavigationState>> usersState = new HashMap<>();

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
		this.telegramClient = new OkHttpTelegramClient(this.botToken);
		usersAccounts = new HashMap<>();
		usersState = new HashMap<>();
		userSessions = new HashMap<>();
	}

	public String getBotUsername() {
		return botUsername;
	}

	public String getBotToken() {
		return botToken;
	}

	public boolean isAccountExist(String telegramId) {
		return usersAccounts.containsKey(telegramId);
	}

	@Override
	public void consume(Update update) {
		if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().contains("/")) {
			botCommandsHandler(update.getMessage());
		} else if (update.hasCallbackQuery()) {

		}

		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			// Set variables
			String message_text = update.getMessage().getText();
			long chat_id = update.getMessage().getChatId();

			try {
				sendTextMessage(chat_id, message_text);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
	}

	private void botCommandsHandler(Message message) {
		String command = message.getText();

		switch (command) {
//			case "/start" -> viewDashboard();
//			case "/order" -> issueOrder();
//			case "/cancel" -> cancelPendingOrder();
//			case "/manage_orders" -> viewManageOrdersMenu();
//			case "/settings" -> viewSettingsMenu();
			case "/logOut" -> logoutHandler(message.getFrom().getId());
			case "/help" -> viewHelpPage();
		}

	}

	private void logoutHandler(Long accountId) {
		accountId


		Account userAccount = usersAccounts.get(accountId);
		if (userAccount == null || userAccount.logOut()) {
			String msg = "\u26a0\ufe0f *_Warning:_* You are not logged in\\!";
			try {
				sendTextMessage(accountId, msg);
			} catch (TelegramApiException e) {
				throw new RuntimeException(e);
			}
			return;
		}

		if (userAccount.isAuthenticated()) userAccount.logOut();
		String msg = "\u2705 *_You have log out successfully. See you soon\\! \\ud83d\\udc4b_*";
		try {
			sendTextMessage(accountId, msg);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	private void viewHelpPage() {

	}

	private void sendTextMessage(long user, String textMessage) throws TelegramApiException{
		SendMessage msg = SendMessage
				.builder()
				.chatId(user)
				.text(textMessage)
				.parseMode("MarkdownV2")
				.build();

		telegramClient.execute(msg);
	}
}