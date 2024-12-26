package org.quickbitehub;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.client.Account;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.utils.MessageHandler;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Stack;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	private final TelegramClient telegramClient;
	private final MessageHandler communicator;

	public static final HashMap<Long, Stack<NavigationState>> sessionState = new HashMap<>(); // TelegramId -> State

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
		this.telegramClient = new OkHttpTelegramClient(this.botToken);
		this.communicator = new MessageHandler(this.telegramClient);
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
		if (update.hasMessage()) {
			Message msg = update.getMessage();
			if (msg.isCommand()) botCommandsHandler(msg);
//			if (msgText.contains("@"+botUsername.toLowerCase())) botRepliesHandler(update.getMessage());
			if (msg.isReply()) botRepliesHandler(msg);
		} else if (update.hasCallbackQuery()) botQueryHandler(update.getCallbackQuery());
	}

	private void botCommandsHandler(Message message) {
		String command = message.getText();
		Long telegramId = message.getFrom().getId();

		switch (command) {
			case "/start" -> viewDashboard(telegramId);
//			case "/order" -> issueOrder();
//			case "/cancel" -> cancelPendingOrder();
//			case "/manage_orders" -> viewManageOrdersMenu();
//			case "/settings" -> viewSettingsMenu();
			case "/logout" -> Authentication.signOut(telegramId);
			case "/help" -> viewHelpPage();
		}
	}


	private void botRepliesHandler(Message message) {
		Authentication.signIn(message, message.getChat().getId());
		Authentication.signUp(message, message.getChat().getId());
	}

	private void botQueryHandler(CallbackQuery cbq) {
		String cbqData = cbq.getData();
		Long telegramId = cbq.getFrom().getId();

		if (cbqData.equals(CBQData.SIGNING_PROCESS.getData())) {
			Authentication.signIn(null, telegramId);
		} else if (cbqData.equals(CBQData.SIGNUP_PROCESS.getData())) {
			Authentication.signUp(null, telegramId);
		}
	}

	private void viewDashboard(Long telegramId) {
		if (Authentication.userSessions.get(telegramId) == null) {
			Authentication.authenticate(telegramId);
			return;
		}

		// show dashboard menu
	}


	private void viewHelpPage() {

	}


}