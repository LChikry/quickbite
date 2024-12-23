package org.quickbitehub;

import org.quickbitehub.client.Account;
import org.quickbitehub.client.Customer;
import org.quickbitehub.client.NavigationState;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.client.UserType;
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

	private void getReplySendNextPrompt(Message message, Long telegramId, HashMap<String, Object> userProcessInfo, String msg, String txt, String nextMsgKey, String nextMsgPrompt) {
		if (userProcessInfo.get(msg) == null) return;
		Message enteredEmail = (Message) userProcessInfo.get(msg);
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) return;

		userProcessInfo.put(txt, message.getText().trim().strip());
		communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		communicator.deleteMessage(telegramId, message.getMessageId());

		Message nextMessage = communicator.sendForceReply(telegramId, nextMsgPrompt);
		userProcessInfo.put(nextMsgKey, nextMessage);
		inProgressInformation.put(telegramId, userProcessInfo);
	}

	private void botRepliesHandler(Message message) {
		Long telegramId = message.getFrom().getId();
		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNING_EMAIL_MSG.getKey(),
				KeyConstant.SIGNING_EMAIL_TXT.getKey(),
				KeyConstant.SIGNING_PASSWORD_MSG.getKey(),
				"Enter Password\\:"
		);

		var userProcessInfo = inProgressInformation.get(telegramId);
		if (userProcessInfo.get(KeyConstant.SIGNING_PASSWORD_MSG.getKey()) != null) {
			Message enteredPassword = (Message) userProcessInfo.get(KeyConstant.SIGNING_PASSWORD_MSG.getKey());
			if (Objects.equals(message.getReplyToMessage().getMessageId(), enteredPassword.getMessageId())) {
				String password = message.getText();
				communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
				communicator.deleteMessage(telegramId, message.getMessageId());

				String email = (String) userProcessInfo.get(KeyConstant.SIGNING_EMAIL_TXT.getKey());
				signInHandler(telegramId, email.toLowerCase(), password, CBQData.SIGNING_VERIFICATION.getData());
			}
		}

		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNUP_EMAIL_MSG.getKey(),
				KeyConstant.SIGNUP_EMAIL_TXT.getKey(),
				KeyConstant.SIGNUP_PASSWORD_MSG.getKey(),
				"Enter Password\\:"
		);

		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNUP_PASSWORD_MSG.getKey(),
				KeyConstant.SIGNUP_PASSWORD_TXT.getKey(),
				KeyConstant.SIGNUP_ID_MSG.getKey(),
				"Enter University ID\\:"
		);

		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNUP_ID_MSG.getKey(),
				KeyConstant.SIGNUP_ID_TXT.getKey(),
				KeyConstant.SIGNUP_FIRST_NAME_MSG.getKey(),
				"Enter First Name\\:"
		);

		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNUP_FIRST_NAME_MSG.getKey(),
				KeyConstant.SIGNUP_FIRST_NAME_TXT.getKey(),
				KeyConstant.SIGNUP_LAST_NAME_MSG.getKey(),
				"Enter Last Name\\:"
		);

		getReplySendNextPrompt(message, telegramId, inProgressInformation.get(telegramId),
				KeyConstant.SIGNUP_LAST_NAME_MSG.getKey(),
				KeyConstant.SIGNUP_LAST_NAME_TXT.getKey(),
				KeyConstant.SIGNUP_MIDDLE_NAMES_MSG.getKey(),
				"Enter Middle Names \\(enter 0 if you don\\'t have middle name\\(s\\)\\)"
		);

		userProcessInfo = inProgressInformation.get(telegramId);
		if (userProcessInfo.get(KeyConstant.SIGNUP_MIDDLE_NAMES_MSG.getKey()) != null) {
			Message enteredPassword = (Message) userProcessInfo.get(KeyConstant.SIGNUP_MIDDLE_NAMES_MSG.getKey());
			if (Objects.equals(message.getReplyToMessage().getMessageId(), enteredPassword.getMessageId())) {
				String middle_names = message.getText().strip().trim();
				communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
				communicator.deleteMessage(telegramId, message.getMessageId());

				if (middle_names.equals("0")) middle_names = "";
				String email = (String) userProcessInfo.get(KeyConstant.SIGNUP_EMAIL_TXT.getKey());
				String password = (String) userProcessInfo.get(KeyConstant.SIGNUP_PASSWORD_TXT.getKey());
				String id = (String) userProcessInfo.get(KeyConstant.SIGNUP_ID_TXT.getKey());
				String first_name = (String) userProcessInfo.get(KeyConstant.SIGNUP_FIRST_NAME_TXT.getKey());
				String last_name = (String) userProcessInfo.get(KeyConstant.SIGNUP_LAST_NAME_TXT.getKey());

				signUpHandler(telegramId, email.toLowerCase(), password, id, first_name, last_name, middle_names);
			}
		}


	}

	private void botQueryHandler(CallbackQuery cbq) {
		String cbqData = cbq.getData();
		Long telegramId = cbq.getFrom().getId();

		if (cbqData.equals(CBQData.SIGNING_MENU.getData())) {
			Message msg = communicator.sendForceReply(telegramId, "Enter Email\\:");

			HashMap<String, Object> signIn = new HashMap<>();
			signIn.put(KeyConstant.SIGNING_EMAIL_MSG.getKey(), msg);
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
			signInHandler(telegramId, null, null, CBQData.SIGNING_MENU.getData());
			return;
		}


		// login page
	}

	private void signInHandler(Long telegramId, String email, String password, String flag) {
		if (flag.equals(CBQData.SIGNING_MENU.getData())) {
			String msg = "   *_Login_*\n" +
					"Please enter your university email address and your chosen password\\.\n\n" +
					"If you don\\'t have an account\\, please sign up first \ud83d\ude01";

			communicator.sendButtonKeyboard(telegramId, msg, (InlineKeyboardMarkup) keyboards.get(KeyboardType.LOGIN));
			return;
		}
		if (!Account.isValidEmail(email) || !Account.isAccountExist(email) || userSessions.get(telegramId) != null) {
			communicator.sendText(telegramId, "Sign in process failed \ud83d\ude1e\\; incorrect email or you are already logged in");
			return;
		}
		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			communicator.sendText(telegramId, "Sign in process failed \ud83d\ude1e\\; incorrect credentials");
		} else {
			userSessions.put(telegramId, userAccount);
			communicator.sendText(telegramId, "Welcome\\! \u2728");
		}
	}

	private void signUpHandler(Long telegramId, String email, String password, String id, String first_name, String last_name, String middle_names) {
		if (!Account.isValidEmail(email) || userSessions.get(telegramId) != null) {
			communicator.sendText(telegramId, "Sign up process failed\\; incorrect email \ud83d\ude1e");
			return;
		}
		Account userAccount = Account.signUp(email, password, telegramId, first_name, last_name, middle_names, UserType.CUSTOMER.getText(), null);

		userSessions.put(telegramId, userAccount);
		communicator.sendText(telegramId, "Welcome\\! \u2728");
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
		if (sessionState.get(telegramId) != null) sessionState.get(telegramId).clear();
		String msg = "\u2705 *_You have log out successfully\\. See you soon\\! \ud83d\udc4b_*";
		communicator.sendText(telegramId, msg);
	}

	private void viewHelpPage() {

	}


}