package org.quickbitehub;

import org.quickbitehub.client.Account;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Stack;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	private final TelegramClient telegramClient;
	private final MessageHandler sendMessage;
	private final HashMap<Long, Account> userSessions; // TelegramId -> Account
	private HashMap<Long, Stack<NavigationState>> sessionState = new HashMap<>(); // TelegramId -> State

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
		this.telegramClient = new OkHttpTelegramClient(this.botToken);
		this.sendMessage = new MessageHandler(this.telegramClient);

		sessionState = new HashMap<>();
		userSessions = new HashMap<>();
	}

	public String getBotUsername() {
		return botUsername;
	}

	public String getBotToken() {
		return botToken;
	}

	public boolean isAccountExist(String telegramId) {
		return Account.usersAccount.containsKey(telegramId);
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

			sendMessage.textMessage(chat_id, message_text);
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
			case "/logout" -> logoutHandler(message.getFrom().getId());
			case "/help" -> viewHelpPage();
		}
	}

	private void logoutHandler(Long telegramId) {
		Account userAccount = userSessions.get(telegramId);
		if (userAccount == null) {
			String msg = "\u26a0\ufe0f *_Warning:_* You are not logged in\\!";
			sendMessage.textMessage(telegramId, msg);
			System.out.println("we finished");
			return;
		}

		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSessions.remove(telegramId);
		sessionState.get(telegramId).clear();
		String msg = "\u2705 *_You have log out successfully. See you soon\\! \ud83d\udc4b_*";
		sendMessage.textMessage(telegramId, msg);
	}

	private void viewHelpPage() {

	}


}