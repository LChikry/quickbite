package org.quickbitehub;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.authentication.Account;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.client.Restaurant;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Stack;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	private final TelegramClient telegramClient;

	public static final HashMap<Long, Stack<NavigationState>> sessionState = new HashMap<>(); // TelegramId -> State

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
		this.telegramClient = new OkHttpTelegramClient(this.botToken);
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
			else if (msg.isReply()) botRepliesHandler(msg);
			else if (msg.hasText()) botMessageHandler(msg);
		} else if (update.hasCallbackQuery()) botCallBackQueryHandler(update.getCallbackQuery());
		else if (update.hasInlineQuery()) botInlineQueryHandler(update.getInlineQuery());
	}

	private void botCommandsHandler(Message message) {
		String command = message.getText();
		Long telegramId = message.getFrom().getId();
		if (!command.equals("/logout") && !command.equals("/help") && Authentication.userSessions.get(telegramId) == null) {
			Authentication.authenticate(telegramId);
			return;
		}
		switch (command) {
			case "/start" -> viewDashboard(telegramId);
			case "/order" -> issueOrder(telegramId);
//			case "/cancel" -> cancelPendingOrder();
//			case "/manage_orders" -> viewManageOrdersMenu();
//			case "/settings" -> viewSettingsMenu();
			case "/logout" -> Authentication.signOut(telegramId);
//			case "/help" -> viewHelpPage();
		}
	}

	private void botRepliesHandler(Message message) {
		Authentication.signIn(message, message.getChat().getId());
		Authentication.signUp(message, message.getChat().getId());
	}

	private void botCallBackQueryHandler(CallbackQuery cbq) {
		String cbqData = cbq.getData();
		Long telegramId = cbq.getFrom().getId();
		if (!cbqData.equals(CBQData.SIGNING_PROCESS.getData()) &&
			cbqData.equals(CBQData.SIGNUP_PROCESS.getData()) &&
			Authentication.userSessions.get(telegramId) == null) {

			Authentication.authenticate(telegramId);
			return;
		}

		if (cbqData.equals(CBQData.SIGNING_PROCESS.getData())) {
			Authentication.signIn(null, telegramId);
		} else if (cbqData.equals(CBQData.SIGNUP_PROCESS.getData())) {
			Authentication.signUp(null, telegramId);
		} else if (cbqData.equals(CBQData.ISSUE_ORDER.getData())) {
			issueOrder(telegramId);
		}
	}

	private void botInlineQueryHandler(InlineQuery inlineQuery) {
		Long telegramId = inlineQuery.getFrom().getId();
		String query = inlineQuery.getQuery().strip().trim().toLowerCase();
		if (query.contains("restaurant")) {
			query = query.substring(query.indexOf(":")+2);
			Restaurant.viewRestaurants(telegramId, query);
			return;
		}

		if (query.contains("items")) {
			query = query.substring(query.indexOf(":")+2);
			Restaurant.viewRestaurantProducts(telegramId, query);
			return;
		}
	}

	private void botMessageHandler(Message message) {
		Restaurant restaurant = Restaurant.allRestaurants.get(message.getText());
	}

	public static void viewDashboard(Long telegramId) {
		// show dashboard menu
		// it will show the balance
		// it will show full name
		// it will show in progress orders
	}

	public static void issueOrder(Long telegramId) {
		Restaurant.viewRestaurants(telegramId, null);
		// tasks
		/*
			- choose product
			- choose quantity
			- choose next product
			- confirm
		 */
	}

	private void viewHelpPage() {

	}


}