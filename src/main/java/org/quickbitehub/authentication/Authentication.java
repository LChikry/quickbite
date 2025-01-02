package org.quickbitehub.authentication;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.consumer.UserState;
import org.quickbitehub.utils.KeyboardFactory;
import org.quickbitehub.utils.MessageHandler;
import org.quickbitehub.QuickBite;
import org.quickbitehub.utils.Emoji;
import org.quickbitehub.consumer.UserType;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.Objects;


public class Authentication {
	private static final OkHttpTelegramClient telegramClient = new OkHttpTelegramClient(Dotenv.load().get("BOT_TOKEN"));
	static final HashMap<Long, HashMap<String, Object>> authProcesses = new HashMap<>(); // device(Telegram Account Id) -> Current Step
	public static final HashMap<Long, Account> userSessions = new HashMap<>(); // TelegramId -> Account

	public static void authenticate(Long telegramId) {
		if (isSessionAuthenticated(telegramId)) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed in\\!";
			putAndDeleteAuthFeedbackMessage(telegramId, msg);
			while (!QuickBite.userState.get(telegramId).isEmpty() && QuickBite.userState.get(telegramId).peek().isStateAuthRelated()) {
				QuickBite.userState.get(telegramId).pop();
			}
			if (QuickBite.userState.get(telegramId).isEmpty()) QuickBite.userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
			QuickBite.userState.get(telegramId).push(UserState.BEFORE_NEXT_UPDATE);
			return;
		}

		String msg = "    *_Authenticate_*\n\n" +
					"Welcome to QuickBite, where you can Skip the Line, Save the Time for What Matters Most\\.\n" +
					"Sign in or Sing up, so you can benefit from our services that will streamline your food ordering process for greater life quality \ud83d\ude01";

		Message signingMenu = MessageHandler.sendInlineKeyboard(telegramId, msg, KeyboardFactory.getSignInUpKeyboard(),
				QuickBite.STANDARD_DELAY_TIME_SEC*2);
		HashMap<String, Object> menuStep = new HashMap<>();
		menuStep.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), signingMenu);
		authProcesses.put(telegramId, menuStep);
	}

	private static void getReplySendNextPrompt(Message message, Long telegramId, HashMap<String, Object> userAuthSteps, String msg, String txt, String nextMsgKey, String nextMsgPrompt) {
		if (userAuthSteps == null) return;
		if (userAuthSteps.get(msg) == null) return;
		Message enteredEmail = (Message) userAuthSteps.get(msg);
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) return;

		userAuthSteps.put(txt, message.getText().trim().strip());
		userAuthSteps.remove(msg);
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		MessageHandler.deleteMessage(telegramId, message.getMessageId());

		Message nextMessage = MessageHandler.sendForceReply(telegramId, nextMsgPrompt, QuickBite.STANDARD_DELAY_TIME_SEC);
		userAuthSteps.put(nextMsgKey, nextMessage);
		authProcesses.put(telegramId, userAuthSteps);
	}

	public static void signIn(Message message, Long telegramId) {
		if (isSessionAuthenticated(telegramId)) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed in\\!";
			putAndDeleteAuthFeedbackMessage(telegramId, msg);
			while (!QuickBite.userState.get(telegramId).isEmpty() && QuickBite.userState.get(telegramId).peek().isStateAuthRelated()) {
				QuickBite.userState.get(telegramId).pop();
			}
			if (QuickBite.userState.get(telegramId).isEmpty()) QuickBite.userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
			QuickBite.userState.get(telegramId).push(UserState.BEFORE_NEXT_UPDATE);
			return;
		}

		if (message == null) {
			Message msg = MessageHandler.sendForceReply(telegramId, "Enter Email\\:", QuickBite.STANDARD_DELAY_TIME_SEC);

			HashMap<String, Object> existingProcess = authProcesses.getOrDefault(telegramId, new HashMap<>());
			existingProcess.put(AuthSteps.SIGNING_EMAIL_MSG.getStep(), msg);
			authProcesses.put(telegramId, existingProcess);

			return;
		}

		var userAuthSteps = authProcesses.get(telegramId);
		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNING_EMAIL_MSG.getStep(),
				AuthSteps.SIGNING_EMAIL_TXT.getStep(),
				AuthSteps.SIGNING_PASSWORD_MSG.getStep(),
				"Enter Password\\:"
		);

		if (userAuthSteps.get(AuthSteps.SIGNING_PASSWORD_MSG.getStep()) == null) return;
		Message enteredPassword = (Message) userAuthSteps.get(AuthSteps.SIGNING_PASSWORD_MSG.getStep());
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredPassword.getMessageId())) return;

		String password = message.getText();
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		MessageHandler.deleteMessage(telegramId, message.getMessageId());
		String email = ((String) userAuthSteps.get(AuthSteps.SIGNING_EMAIL_TXT.getStep())).strip().trim().toLowerCase();

		signInHandler(telegramId, email, password);
	}

	private static void signInHandler(Long telegramId, String email, String password) {
		if (!isAuthenticationInformationValid(telegramId, email, password, UserState.AUTHENTICATION_SIGNIN)) return;
		email = Account.formatEmail(email);
		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			String textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
			putAndDeleteAuthFeedbackMessage(telegramId, textMsg);

			var menuMsg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
			HashMap<String, Object> temp = new HashMap<>();
			temp.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), menuMsg);
			authProcesses.remove(telegramId); // the process is finished
			authProcesses.put(telegramId, temp);
			QuickBite.cancelCurrentOperation(telegramId);
			return;
		}

		userSessions.put(telegramId, userAccount);
		Integer msgId = ((Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep())).getMessageId();
		MessageHandler.deleteMessage(telegramId, msgId);

		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You Signed In Successfully " + Emoji.HAND_WAVING.getCode();
		putAndDeleteAuthFeedbackMessage(telegramId, feedbackMsg);
		authProcesses.remove(telegramId); // the process is finished
		while (!QuickBite.userState.get(telegramId).isEmpty() && QuickBite.userState.get(telegramId).peek().isStateAuthRelated()) {
			QuickBite.userState.get(telegramId).pop();
		}
		if (QuickBite.userState.get(telegramId).isEmpty()) QuickBite.userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
		QuickBite.userState.get(telegramId).push(UserState.BEFORE_NEXT_UPDATE);
	}

	public static void signUp(Message message, Long telegramId) {
		if (isSessionAuthenticated(telegramId)) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed in\\!";
			putAndDeleteAuthFeedbackMessage(telegramId, msg);
			while (!QuickBite.userState.get(telegramId).isEmpty() && QuickBite.userState.get(telegramId).peek().isStateAuthRelated()) {
				QuickBite.userState.get(telegramId).pop();
			}
			if (QuickBite.userState.get(telegramId).isEmpty()) QuickBite.userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
			QuickBite.userState.get(telegramId).push(UserState.BEFORE_NEXT_UPDATE);
			return;
		}

		if (message == null) {
			Message msg = MessageHandler.sendForceReply(telegramId, "Enter Email\\:", QuickBite.STANDARD_DELAY_TIME_SEC);

			HashMap<String, Object> existingProcess = authProcesses.getOrDefault(telegramId, new HashMap<>());
			existingProcess.put(AuthSteps.SIGNUP_EMAIL_MSG.getStep(), msg);
			authProcesses.put(telegramId, existingProcess);
			return;
		}

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_EMAIL_MSG.getStep(),
				AuthSteps.SIGNUP_EMAIL_TXT.getStep(),
				AuthSteps.SIGNUP_PASSWORD_MSG.getStep(),
				"Enter Password\\:"
		);

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_PASSWORD_MSG.getStep(),
				AuthSteps.SIGNUP_PASSWORD_TXT.getStep(),
				AuthSteps.SIGNUP_ID_MSG.getStep(),
				"Enter University ID\\:"
		);

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_ID_MSG.getStep(),
				AuthSteps.SIGNUP_ID_TXT.getStep(),
				AuthSteps.SIGNUP_FIRST_NAME_MSG.getStep(),
				"Enter First Name\\:"
		);

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_FIRST_NAME_MSG.getStep(),
				AuthSteps.SIGNUP_FIRST_NAME_TXT.getStep(),
				AuthSteps.SIGNUP_LAST_NAME_MSG.getStep(),
				"Enter Last Name\\:"
		);

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_LAST_NAME_MSG.getStep(),
				AuthSteps.SIGNUP_LAST_NAME_TXT.getStep(),
				AuthSteps.SIGNUP_MIDDLE_NAMES_MSG.getStep(),
				"Enter Middle Name\\(s\\) \\(otherwise send any character\\)"
		);

		var userAuthSteps = authProcesses.get(telegramId);
		if (userAuthSteps == null) return;
		if (userAuthSteps.get(AuthSteps.SIGNUP_MIDDLE_NAMES_MSG.getStep()) == null) return;
		Message enteredPassword = (Message) userAuthSteps.get(AuthSteps.SIGNUP_MIDDLE_NAMES_MSG.getStep());
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredPassword.getMessageId())) return;

		String middleNames = message.getText().strip().trim();
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		MessageHandler.deleteMessage(telegramId, message.getMessageId());
		if (middleNames.length() == 1) middleNames = "";

		String email = (String) userAuthSteps.get(AuthSteps.SIGNUP_EMAIL_TXT.getStep());
		String password = (String) userAuthSteps.get(AuthSteps.SIGNUP_PASSWORD_TXT.getStep());
		String id = (String) userAuthSteps.get(AuthSteps.SIGNUP_ID_TXT.getStep());
		String firstName = (String) userAuthSteps.get(AuthSteps.SIGNUP_FIRST_NAME_TXT.getStep());
		String lastName = (String) userAuthSteps.get(AuthSteps.SIGNUP_LAST_NAME_TXT.getStep());

		signUpHandler(telegramId, email, password, id, firstName, lastName, middleNames);
	}

	private static void signUpHandler(Long telegramId, String email, String password, String id, String firstName, String lastName, String middleNames) {
		if (!isAuthenticationInformationValid(telegramId, email, password, UserState.AUTHENTICATION_SIGNUP)) return;
		if (!Account.isAccountInformationValid(telegramId, firstName, lastName, middleNames)) return;
		email = Account.formatEmail(email);
		firstName = Account.formatName(firstName);
		lastName = Account.formatName(lastName);
		if (!middleNames.equals("")) middleNames = Account.formatName(middleNames);

		Account userAccount = Account.signUp(email, password, telegramId, firstName, lastName, middleNames, UserType.CUSTOMER.getText(), null);
		userSessions.put(telegramId, userAccount);
		Message msg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
		MessageHandler.deleteMessage(telegramId, msg.getMessageId());

		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You've Created Your Account Successfully " + Emoji.HAND_WAVING.getCode();
		putAndDeleteAuthFeedbackMessage(telegramId, feedbackMsg);
		authProcesses.remove(telegramId); // the process is finished
		while (!QuickBite.userState.get(telegramId).isEmpty() && QuickBite.userState.get(telegramId).peek().isStateAuthRelated()) {
			QuickBite.userState.get(telegramId).pop();
		}
		if (QuickBite.userState.get(telegramId).isEmpty()) QuickBite.userState.get(telegramId).push(UserState.DASHBOARD_PAGE);
		QuickBite.userState.get(telegramId).push(UserState.BEFORE_NEXT_UPDATE);
	}

	private static boolean isAuthenticationInformationValid(Long telegramId, String email, String password, UserState processType) {
		if (processType == UserState.AUTHENTICATION_SIGNIN) {
			if (Account.isEmailValid(email) &&
					Account.isAccountExist(Account.formatEmail(email)) &&
					userSessions.get(telegramId) == null) {
				Account userAccount = Account.authenticate(telegramId, Account.formatEmail(email), password);
				return userAccount != null;
			}
		} else {
			if (Account.isEmailValid(email) &&
					!Account.isAccountExist(Account.formatEmail(email)) &&
					userSessions.get(telegramId) == null) {
				return true;
			}
		}

		String textMsg = "";
		if (userSessions.get(telegramId) != null) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign In/Up Failed*\nYou are already signed in " + Emoji.SAD_FACE.getCode();
		} else if (!Account.isEmailValid(email)) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In/Up Failed*\nInvalid Email " + Emoji.SAD_FACE.getCode();
		} else if (Account.isAccountExist(Account.formatEmail(email))) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign Up Failed*\nYou Already Have an Account; Just Sign In " + Emoji.SMILING_FACE.getCode();
		} else if (Account.authenticate(telegramId, Account.formatEmail(email), password) == null) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
		}
		putAndDeleteAuthFeedbackMessage(telegramId, textMsg);

		var menuMsg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
		HashMap<String, Object> temp = new HashMap<>();
		temp.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), menuMsg);
		authProcesses.remove(telegramId); // the process is finished
		authProcesses.put(telegramId, temp);
		QuickBite.cancelCurrentOperation(telegramId);
		return false;
	}

	public static void signOut(Long telegramId) {
		Account userAccount = userSessions.get(telegramId);
		if (userAccount == null) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed out\\!";
			putAndDeleteAuthFeedbackMessage(telegramId, msg);
			return;
		}

		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSessions.remove(telegramId);
		String msg = Emoji.GREEN_CIRCLE.getCode() + " *_You have log out successfully\\. See you soon\\! \ud83d\udc4b_*";
		putAndDeleteAuthFeedbackMessage(telegramId, msg);
	}

	public static boolean isSessionAuthenticated(Long telegramId) {
		return userSessions.get(telegramId) != null;
	}

	public static Account getSessionAccount(Long telegramId) {
		return userSessions.get(telegramId);
	}

	static void putAndDeleteAuthFeedbackMessage(Long telegramId, String textMessage) {
		Message fm = MessageHandler.sendText(telegramId, textMessage, QuickBite.SHORT_DELAY_TIME_SEC);
	}
}
