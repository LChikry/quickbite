package org.quickbitehub;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.authentication.Account;
import org.quickbitehub.client.UserState;

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

	public static final HashMap<Long, Stack<UserState>> userState = new HashMap<>(); // TelegramId -> State

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
			if (userState.get(telegramId) != null && !userState.get(telegramId).isEmpty()) userState.get(telegramId).clear();
			Stack<UserState> userStates = new Stack<>();
			userStates.push(UserState.getValueOf(command));
			userStates.push(UserState.AUTHENTICATION_PROCESS);
			userState.put(telegramId, userStates);
			Authentication.authenticate(telegramId);
			return;
		}
		switch (UserState.getValueOf(command)) {
			case UserState.DASHBOARD_PAGE -> viewDashboard(telegramId);
			case UserState.CANCEL_CURRENT_OPERATION -> cancelCurrentOperation(telegramId);
			case UserState.ISSUING_ORDER_PROCESS -> issueOrder(telegramId);
//			case UserState.CANCEL_PENDING_ORDER -> cancelPendingOrder();
//			case UserState.MANAGE_ORDERS_PAGE -> viewManageOrdersPage();
//			case UserState.SETTINGS_PAGE -> viewSettingsPage();
			case UserState.LOGOUT -> Authentication.signOut(telegramId);
//			case UserState.HELP_PAGE -> viewHelpPage();
		}
	}

	private void cancelCurrentOperation(Long telegramId) {
		userState.get(telegramId).clear();
		navigateToProperState(telegramId);
	}

	private void botRepliesHandler(Message message) {
		Authentication.signIn(message, message.getChat().getId());
		Authentication.signUp(message, message.getChat().getId());
	}

	private void botCallBackQueryHandler(CallbackQuery cbq) {
		String cbqData = cbq.getData();
		Long telegramId = cbq.getFrom().getId();
		if (!cbqData.equals(CBQData.SIGNING_PROCESS.getData()) &&
			!cbqData.equals(CBQData.SIGNUP_PROCESS.getData()) &&
			Authentication.userSessions.get(telegramId) == null) {

			if (userState.get(telegramId) != null && !userState.get(telegramId).isEmpty()) userState.get(telegramId).clear();
			Stack<UserState> userStates = new Stack<>();
			userStates.push(UserState.getValueOf(cbqData));
			userStates.push(UserState.AUTHENTICATION_PROCESS);
			userState.put(telegramId, userStates);
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
			if (userState.get(telegramId).peek() != UserState.ISSUING_ORDER_PROCESS) {
				userState.get(telegramId).push(UserState.ISSUING_ORDER_PROCESS);
			}
			if (!Restaurant.viewRestaurants(telegramId, query)) {
				QuickBite.userState.get(telegramId).pop();
				QuickBite.navigateToProperState(telegramId);
			}
			return;
		}

		if (query.contains("items")) {
			query = query.substring(query.indexOf(":")+2);
			Restaurant.viewRestaurantProducts(telegramId, query);
			return;
		}
	}

	private void botMessageHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		Restaurant restaurant = Restaurant.allRestaurants.get(message.getText());
		if (restaurant == null ||
				userState.get(telegramId) == null ||
				userState.get(telegramId).isEmpty() ||
				userState.get(telegramId).peek() != UserState.CHOOSING_PRODUCTS) {
			return;
		}
		System.out.println("we detected a restaurant");
		// task: show the available products and ask for the quantity for each product
		userState.get(telegramId).pop();
	}

	public static void viewDashboard(Long telegramId) {
		// show dashboard menu
		// it will show the balance
		// it will show full name
		// it will show in progress orders
	}

	public static void issueOrder(Long telegramId) {
		if (userState.get(telegramId) == null ||
				userState.get(telegramId).isEmpty() ||
				userState.get(telegramId).peek() != UserState.ISSUING_ORDER_PROCESS) {
			userState.get(telegramId).push(UserState.ISSUING_ORDER_PROCESS);
			if(!Restaurant.viewRestaurants(telegramId, null)) {
				QuickBite.userState.get(telegramId).pop();
				QuickBite.navigateToProperState(telegramId);
				return;
			}
			QuickBite.userState.get(telegramId).pop();
			QuickBite.userState.get(telegramId).push(UserState.CHOOSING_PRODUCTS);
			return;
		}
		UserState userState = QuickBite.userState.get(telegramId).peek();
		if (userState == UserState.ISSUING_ORDER_PROCESS) {
			QuickBite.userState.get(telegramId).push(UserState.CHOOSING_PRODUCTS);
			// choose products and quantity
			return;
		}

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

	public static void navigateToProperState(Long telegramId) {
		if (userState.get(telegramId) == null || userState.get(telegramId).isEmpty()) {
			userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
			viewDashboard(telegramId);
			return;
		}

		UserState properState = userState.get(telegramId).pop();
		switch (properState) {
			case UserState.DASHBOARD_PAGE -> viewDashboard(telegramId);
			case UserState.ISSUING_ORDER_PROCESS -> issueOrder(telegramId);
//			case UserState.CANCEL_PENDING_ORDER -> cancelPendingOrder();
//			case UserState.MANAGE_ORDERS_PAGE -> viewManageOrdersPage();
//			case UserState.SETTINGS_PAGE -> viewSettingsPage();
			case UserState.CHOOSING_PRODUCTS -> issueOrder(telegramId);
		}
	}
}