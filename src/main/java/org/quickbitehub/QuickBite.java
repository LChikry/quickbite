package org.quickbitehub;

import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.consumer.UserState;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.order.Order;
import org.quickbitehub.order.OrderStatus;
import org.quickbitehub.provider.Restaurant;
import org.quickbitehub.utils.Emoji;
import org.quickbitehub.utils.KeyboardFactory;
import org.quickbitehub.utils.MessageHandler;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Instant;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	public static final HashMap<Long, Stack<UserState>> userState = new HashMap<>(); // TelegramId -> State
	public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static final long STANDARD_DELAY_TIME_SEC = 30; // in seconds
	public static final long LONG_DELAY_TIME_SEC = 90; // in seconds
	public static final long SHORT_DELAY_TIME_SEC = 8; // in seconds

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
	}

	@Override
	public void consume(Update update) {
		long telegramId = -1;
		if (update.hasMessage()) {
			Message msg = update.getMessage();
			telegramId = msg.getChatId();
			if (msg.getDate() + 20 < Instant.now().getEpochSecond()) {
				MessageHandler.deleteMessage(telegramId, msg.getMessageId());
				return;
			}
			if (!userState.containsKey(telegramId)) userState.put(telegramId, new Stack<>());
			if (msg.isCommand()) botCommandsHandler(msg);
			else if (msg.isReply()) botRepliesHandler(msg);
			else if (msg.hasText()) botMessageHandler(msg);
		} else if (update.hasCallbackQuery()) {
			telegramId = update.getCallbackQuery().getFrom().getId();
			if (!userState.containsKey(telegramId)) userState.put(telegramId, new Stack<>());
			botCallBackQueryHandler(update.getCallbackQuery());
		} else if (update.hasInlineQuery()) botInlineQueryHandler(update.getInlineQuery());
		
		if (telegramId != -1 && !userState.get(telegramId).isEmpty() && userState.get(telegramId).peek() == UserState.BEFORE_NEXT_UPDATE) {
			userState.get(telegramId).pop();
			navigateToProperState(telegramId);
		}
	}

	private void botCommandsHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		UserState newState = UserState.getValueOf(message.getText());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), STANDARD_DELAY_TIME_SEC);
		Stack<UserState> stack = userState.get(telegramId);
		if (stack.isEmpty() ||
				Authentication.isSessionAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated() ||
				!stack.peek().isStateAuthRelated()) {
			stack.push(newState);
		}
		navigateToProperState(telegramId);
	}

	private void botCallBackQueryHandler(CallbackQuery cbq) {
		Long telegramId = cbq.getFrom().getId();
		UserState newState = UserState.getValueOf(cbq.getData());
		Stack<UserState> stack = userState.get(telegramId);
		if (stack.isEmpty() ||
				Authentication.isSessionAuthenticated(telegramId) ||
				newState.isImmediateState() ||
				newState.isStateAuthRelated()) {
			stack.push(newState);
		}
		navigateToProperState(telegramId);
	}

	private void botRepliesHandler(Message message) {
		UserState currentState = userState.get(message.getChatId()).peek();
		switch (currentState) {
			case AUTHENTICATION_SIGNIN -> Authentication.signIn(message, message.getChat().getId());
			case AUTHENTICATION_SIGNUP -> Authentication.signUp(message, message.getChat().getId());
		}
	}

	private void botInlineQueryHandler(InlineQuery inlineQuery) {
		Long telegramId = inlineQuery.getFrom().getId();
		String query = inlineQuery.getQuery().strip().trim().toLowerCase();
		if (query.contains("restaurant")) {
			query = query.substring(query.indexOf(":")+2);
			if (userState.get(telegramId).peek() != UserState.IO_RESTAURANT_SELECTION) {
				userState.get(telegramId).push(UserState.IO_RESTAURANT_SELECTION);
			}
			QuickBite.userState.get(telegramId).pop();
			if (!Restaurant.viewRestaurants(telegramId, query)) {
				QuickBite.navigateToProperState(telegramId);
			} else {
				QuickBite.userState.get(telegramId).push(UserState.IO_PRODUCTS_SELECTION);
			}
			return;
		}

		if (query.contains("items")) {
			query = query.substring(query.indexOf(":")+2);
			Restaurant.viewRestaurantProducts(telegramId, query);
			if (userState.get(telegramId).peek() != UserState.IO_PRODUCTS_SELECTION) {
				userState.get(telegramId).push(UserState.IO_PRODUCTS_SELECTION);
			}
			return;
		}
	}

	private void botMessageHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		Restaurant restaurant = Restaurant.allRestaurants.get(message.getText());
		if (restaurant == null ||
				userState.get(telegramId) == null ||
				userState.get(telegramId).isEmpty() ||
				userState.get(telegramId).peek() != UserState.IO_PRODUCTS_SELECTION) {
			return;
		}
		System.out.println("we detected a restaurant");
		// task: show the available products and ask for the quantity for each product
		userState.get(telegramId).pop();
	}

	public static void navigateToProperState(Long telegramId) {
		assert (userState.get(telegramId) != null);
		Stack<UserState> stack = userState.get(telegramId);
		if (stack.isEmpty()) {
			if (Authentication.isSessionAuthenticated(telegramId)) stack.push(UserState.DASHBOARD_PAGE);
			else stack.push(UserState.AUTHENTICATION_NEEDED);
		} else if (!Authentication.isSessionAuthenticated(telegramId) &&
				!stack.peek().isStateAuthRelated() &&
				!stack.peek().isImmediateState()) {
			stack.push(UserState.AUTHENTICATION_NEEDED);
		}

		UserState properState = stack.peek();
		switch (properState) {
			case AUTHENTICATION_NEEDED -> Authentication.authenticate(telegramId);
			case AUTHENTICATION_SIGNIN -> Authentication.signIn(null, telegramId);
			case AUTHENTICATION_SIGNUP -> Authentication.signUp(null, telegramId);
			case DASHBOARD_PAGE -> viewDashboardPage(telegramId);
			case CANCEL_CURRENT_OPERATION -> cancelCurrentOperation(telegramId);
			case ISSUE_ORDER -> Order.issueOrder(telegramId);
			case IO_RESTAURANT_SELECTION -> {
				if (2 != userState.get(telegramId).search(UserState.ISSUE_ORDER)) cancelCurrentOperation(telegramId);
				else Order.issueOrder(telegramId);
			}
			case UserState.IO_PRODUCTS_SELECTION -> {
				if (2 != userState.get(telegramId).search(UserState.IO_RESTAURANT_SELECTION)) cancelCurrentOperation(telegramId);
				else Order.issueOrder(telegramId);
			}
			case UserState.IO_CONFIRMATION -> {
				if (2 != userState.get(telegramId).search(UserState.IO_PRODUCTS_SELECTION)) cancelCurrentOperation(telegramId);
				else Order.issueOrder(telegramId);
			}
//			case CANCEL_PENDING_ORDER -> cancelPendingOrder();
//			case MANAGE_ORDERS_PAGE -> viewManageOrdersPage();
//			case SETTINGS_PAGE -> viewSettingsPage();
			case AUTHENTICATION_SIGNOUT -> {
				Authentication.signOut(telegramId);
				userState.get(telegramId).clear();
			}
			case HELP_PAGE -> {
				viewHelpPage(telegramId);
				userState.get(telegramId).pop();
			}
		}
	}

	public static void viewDashboardPage(Long telegramId) {
		assert (Authentication.isSessionAuthenticated(telegramId));
		String message = "*Welcome to Dashboard* " + Emoji.YELLOW_STARS.getCode() +
				"\n" +
				"\n*" + Authentication.getSessionAccount(telegramId).getUser().getUserFullName() + "*\\, from here you can take control of everything\\!" +
				"\n" +
				"\nMoney spent so far\\: *" + "*" +
				"\n" +
				"\n" + OrderStatus.PENDING.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.IN_PREPARATION.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.READY.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.CANCELED.getStatus() + " orders\\: *" + "*";

		MessageHandler.sendInlineKeyboard(telegramId, message, KeyboardFactory.getDashboardPageKeyboard(), LONG_DELAY_TIME_SEC);
	}

	public static void cancelCurrentOperation(Long telegramId) {
		userState.get(telegramId).clear();
//		if (!Authentication.isSessionAuthenticated(telegramId)) {
//			userState.get(telegramId).push(UserState.AUTHENTICATION_NEEDED);
//		}
	}

	private static void viewHelpPage(Long telegramId) {
		// task: correct urls as soon as you create the documentation
		String msg = "_*Support Page*_" +
				"\n" +
				"\n    *How do I use QuickBite\\?*" +
				"\nYou can watch tutorials on YouTube or you can read the documentation [for users](www.google.com) or [for restaurant owners](www.google.com)\\." +
				"\n" +
				"\n    *What are the transaction and operation fees\\?*" +
				"\nQuickBite charges a 00\\.00% fees in all kind of operations and transactions\\. QuickBite gets its funding solely from donations\\. However\\, services that may be used during order payment may charge fees\\, which is out of our control\\." +
				"\n" +
				"\n    *Why order can\\'t be canceled\\?*" +
				"\nWhen you issue an order\\, and this order has been accepted by the restaurant\\, you cannot cancel the order anymore since this behavior will damage restaurants\\. However\\, you can cancel pending orders that have been not answered yet by the restaurant\\." +
				"\n" +
				"\n" +
				"\n_If you still have questions\\, or you encountered a problem\\, please do not hesitate to look at the documentation or contact us at *support@quickbitehub\\.org*_";

		MessageHandler.sendInlineKeyboard(telegramId, msg, KeyboardFactory.getHelpPageKeyboard(), LONG_DELAY_TIME_SEC*2);
	}

	public String getBotUsername() {
		return botUsername;
	}

	public String getBotToken() {
		return botToken;
	}
}