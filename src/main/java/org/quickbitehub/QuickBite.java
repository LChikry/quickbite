package org.quickbitehub;

import org.quickbitehub.client.Account;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

import static org.quickbitehub.MessageHandler.keyboards;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
	private final String botToken;
	private final String botUsername = "QuickBiteHub_bot";
	private final TelegramClient telegramClient;
	private final MessageHandler communicator;
	private final HashMap<Long, Account> userSessions; // TelegramId -> Account
	private HashMap<Long, Stack<NavigationState>> sessionState = new HashMap<>(); // TelegramId -> State
	private HashMap<Long, HashMap<String, Object>> inProgressInformation;

	public QuickBite() {
		Dotenv dotenv = Dotenv.load();
		this.botToken = dotenv.get("BOT_TOKEN");
		this.telegramClient = new OkHttpTelegramClient(this.botToken);
		this.communicator = new MessageHandler(this.telegramClient);

		sessionState = new HashMap<>();
		userSessions = new HashMap<>();
		inProgressInformation = new HashMap<>();
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
			case "/logout" -> logOutHandler(telegramId);
			case "/help" -> viewHelpPage();
		}
	}

	private void botRepliesHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		var userProcessInfo = inProgressInformation.get(telegramId);

		if (userProcessInfo.get(KeyConstant.SIGNIN_EMAIL_MSG.getKey()) != null) {
			Message enteredEmail = (Message) userProcessInfo.get(KeyConstant.SIGNIN_EMAIL_MSG.getKey());
			if (Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) {
				userProcessInfo.put(KeyConstant.SIGNIN_EMAIL_TXT.getKey(), message.getText()); // save the email
				communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
				communicator.deleteMessage(telegramId, message.getMessageId());
				inProgressInformation.put(telegramId, userProcessInfo); // save the email

				Message msg = communicator.sendForceReply(telegramId, "Enter Password\\:");
				userProcessInfo.put(KeyConstant.SIGNIN_PASSWORD_MSG.getKey(), msg);
				inProgressInformation.put(telegramId, userProcessInfo);
			}
		}

		if (userProcessInfo.get(KeyConstant.SIGNIN_PASSWORD_MSG.getKey()) != null) {
			Message enteredPass = (Message) userProcessInfo.get(KeyConstant.SIGNIN_PASSWORD_MSG.getKey());
			if (Objects.equals(message.getReplyToMessage().getMessageId(), enteredPass.getMessageId())) {
				String password = message.getText();
				communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
				communicator.deleteMessage(telegramId, message.getMessageId());
				String email = ((String) userProcessInfo.get(KeyConstant.SIGNIN_EMAIL_TXT.getKey())).trim().strip().toLowerCase();
				SignInHandler(telegramId, email, password, CBQData.SIGNING_VERIFICATION.getData());
			}
		}

		if (userProcessInfo.get(KeyConstant.SIGNUP_EMAIL_MSG.getKey()) != null) {
			Message enteredEmail = (Message) userProcessInfo.get(KeyConstant.SIGNIN_EMAIL_MSG.getKey());
			if (Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) {
				userProcessInfo.put(KeyConstant.SIGNIN_EMAIL_TXT.getKey(), message.getText()); // save the email
				communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
				communicator.deleteMessage(telegramId, message.getMessageId());
				inProgressInformation.put(telegramId, userProcessInfo); // save the email

				Message msg = communicator.sendForceReply(telegramId, "Enter Password\\:");
				userProcessInfo.put(KeyConstant.SIGNIN_PASSWORD_MSG.getKey(), msg);
				inProgressInformation.put(telegramId, userProcessInfo);
			}
		}


	}

	private void botQueryHandler(CallbackQuery cbq) {
		String cbqData = cbq.getData();
		Long telegramId = cbq.getFrom().getId();

		if (cbqData.equals(CBQData.SIGNING_MENU.getData())) {
			Message msg = communicator.sendForceReply(telegramId, "Enter Email\\:");

			HashMap<String, Object> signIn = new HashMap<>();
			signIn.put(KeyConstant.SIGNIN_EMAIL_MSG.getKey(), msg);
			inProgressInformation.put(telegramId, signIn);
		} else if (cbqData.equals(CBQData.SIGN_UP_MENU.getData())) {
			Message msg = communicator.sendForceReply(telegramId, "Enter University Email\\:");

			HashMap<String, Object> signUn = new HashMap<>();
			signUn.put(KeyConstant.SIGNUP_EMAIL_MSG.getKey(), msg);
			inProgressInformation.put(telegramId, signUn);
		}
	}

	private void viewDashboard(Long telegramId) {
		if (userSessions.get(telegramId) == null) {
			SignInHandler(telegramId, null, null, CBQData.SIGNING_MENU.getData());
			return;
		}


		// login page
	}

	private void SignInHandler(Long telegramId, String email, String password, String flag) {
		if (flag.equals(CBQData.SIGNING_MENU)) {
			String msg = "   *_Login_*\n" +
					"Please enter your university email address and your chosen password\\.\n\n" +
					"If you don't have an account\\, please sign up first \ud83d\ude01";

			communicator.sendButtonKeyboard(telegramId, msg, (InlineKeyboardMarkup) keyboards.get(KeyboardType.LOGIN));
			return;
		}

		if (!Account.isValidEmail(email) || Account.isAccountExist(email) || userSessions.get(telegramId) == null) {
			communicator.sendText(telegramId, "Sign in process failed \ud83d\ude1e");
			return;
		}
		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			communicator.sendText(telegramId, "Sign in process failed \ud83d\ude1e");
		} else {
			userSessions.put(telegramId, userAccount);
			communicator.sendText(telegramId, "Welcome\\! \u2728");
		}
	}

	private void logOutHandler(Long telegramId) {
		Account userAccount = userSessions.get(telegramId);
		if (userAccount == null) {
			String msg = "\u26a0\ufe0f *_Warning:_* You are not logged in\\!";
			communicator.sendText(telegramId, msg);
			return;
		}

		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSessions.remove(telegramId);
		sessionState.get(telegramId).clear();
		String msg = "\u2705 *_You have log out successfully. See you soon\\! \ud83d\udc4b_*";
		communicator.sendText(telegramId, msg);
	}

	private void viewHelpPage() {

	}


}