package org.quickbitehub.authentication;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.KeyboardType;
import org.quickbitehub.MessageHandler;
import org.quickbitehub.QuickBite;
import org.quickbitehub.SignEmoji;
import org.quickbitehub.client.Account;
import org.quickbitehub.client.UserType;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.Objects;

import static org.quickbitehub.MessageHandler.keyboards;

public class Authentication {
	private static final OkHttpTelegramClient telegramClient = new OkHttpTelegramClient(Dotenv.load().get("BOT_TOKEN"));
	private static final MessageHandler communicator = new MessageHandler(telegramClient);
	private static final HashMap<Long, HashMap<String, Object>> authProcesses = new HashMap<>(); // device(Telegram Account Id) -> Current Step
	public static final HashMap<Long, Account> userSessions = new HashMap<>(); // TelegramId -> Account

	public static void authenticate(Long telegramId) {
		String msg = "    *_Authenticate_*\n" +
					"Welcome to QuickBite, where you can Skip the Line, Save the Time for What Matters Most\\.\n\n" +
					"Here, you can streamline your food ordering process for greater life quality\\.\n" +
					"Authenticate yourself to start benefiting from our services \ud83d\ude01";

		Message signingMenu = communicator.sendButtonKeyboard(telegramId, msg, (InlineKeyboardMarkup) keyboards.get(KeyboardType.SIGN_ING_UP));
		HashMap<String, Object> menuStep = new HashMap<>();
		menuStep.put(AuthSteps.SIGN_ING_UP_MENU.getStep(), signingMenu);
		authProcesses.put(telegramId, menuStep);
	}

	private static void getReplySendNextPrompt(Message message, Long telegramId, HashMap<String, Object> userProcessInfo, String msg, String txt, String nextMsgKey, String nextMsgPrompt) {
		if (userProcessInfo.get(msg) == null) return;
		Message enteredEmail = (Message) userProcessInfo.get(msg);
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) return;

		userProcessInfo.put(txt, message.getText().trim().strip());
		communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		communicator.deleteMessage(telegramId, message.getMessageId());

		Message nextMessage = communicator.sendForceReply(telegramId, nextMsgPrompt);
		userProcessInfo.put(nextMsgKey, nextMessage);
		authProcesses.put(telegramId, userProcessInfo);
	}

	public static void signIn(Message message, Long telegramId) {
		if (message == null) {
			Message msg = communicator.sendForceReply(telegramId, "Enter Email\\:");

			HashMap<String, Object> signIn = new HashMap<>();
			signIn.put(AuthSteps.SIGNING_EMAIL_MSG.getStep(), msg);
			authProcesses.put(telegramId, signIn);
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
		communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		communicator.deleteMessage(telegramId, message.getMessageId());
		authProcesses.remove(telegramId); // clear the process
		String email = ((String) userAuthSteps.get(AuthSteps.SIGNING_EMAIL_TXT.getStep())).strip().trim().toLowerCase();

		signInHandler(telegramId, email, password);
	}

	private static void signInHandler(Long telegramId, String email, String password) {
		if (!Account.isValidEmail(email) || !Account.isAccountExist(email) || userSessions.get(telegramId) != null) {
			deleteRecentAuthFeedbackMessage(telegramId);
			String textMsg = SignEmoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nInvalid Email or You are Logged In \ud83d\ude1e";
			putRecentAuthFeedbackMessage(telegramId, textMsg);
			return;
		}

		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			deleteRecentAuthFeedbackMessage(telegramId);
			String textMsg = SignEmoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password \ud83d\ude1e";
			putRecentAuthFeedbackMessage(telegramId, textMsg);
			return;
		}

		userSessions.put(telegramId, userAccount);
		deleteRecentAuthFeedbackMessage(telegramId);
		String feedbackMsg = SignEmoji.GREEN_CIRCLE.getCode() + " You Signed In Successfully \ud83d\udc4b";
		putRecentAuthFeedbackMessage(telegramId, feedbackMsg);
		try { Thread.sleep(2000);} catch (InterruptedException e) { throw new RuntimeException(e);}
		deleteRecentAuthFeedbackMessage(telegramId);

		Integer msgId = ((Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_ING_UP_MENU.getStep())).getMessageId();
		communicator.deleteMessage(telegramId, msgId);
		// task: show dashboard
	}

	public static void signUp(Message message, Long telegramId) {
		if (message == null) {
			Message msg = communicator.sendForceReply(telegramId, "Enter Email\\:");

			HashMap<String, Object> signUpStep = new HashMap<>();
			signUpStep.put(AuthSteps.SIGNUP_EMAIL_MSG.getStep(), msg);
			authProcesses.put(telegramId, signUpStep);
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
				"Enter Middle Name\\(s\\) \\(or 0)\\)"
		);

		var userAuthSteps = authProcesses.get(telegramId);
		if (userAuthSteps.get(AuthSteps.SIGNUP_MIDDLE_NAMES_MSG.getStep()) == null) return;
		Message enteredPassword = (Message) userAuthSteps.get(AuthSteps.SIGNUP_MIDDLE_NAMES_MSG.getStep());
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredPassword.getMessageId())) return;

		String middle_names = message.getText().strip().trim();
		communicator.deleteMessage(telegramId, message.getReplyToMessage().getMessageId());
		communicator.deleteMessage(telegramId, message.getMessageId());
		authProcesses.remove(telegramId); // the process is finished
		if (middle_names.equals("0")) middle_names = "";

		String email = (String) userAuthSteps.get(AuthSteps.SIGNUP_EMAIL_TXT.getStep());
		String password = (String) userAuthSteps.get(AuthSteps.SIGNUP_PASSWORD_TXT.getStep());
		String id = (String) userAuthSteps.get(AuthSteps.SIGNUP_ID_TXT.getStep());
		String first_name = (String) userAuthSteps.get(AuthSteps.SIGNUP_FIRST_NAME_TXT.getStep());
		String last_name = (String) userAuthSteps.get(AuthSteps.SIGNUP_LAST_NAME_TXT.getStep());

		signUpHandler(telegramId, email.toLowerCase(), password, id, first_name, last_name, middle_names);
	}

	private static void signUpHandler(Long telegramId, String email, String password, String id, String first_name, String last_name, String middle_names) {
		if (!Account.isValidEmail(email) || Account.isAccountExist(email) || userSessions.get(telegramId) != null) {
			deleteRecentAuthFeedbackMessage(telegramId);
			String textMsg = SignEmoji.RED_CIRCLE.getCode() + " *Sign Up Failed*\nInvalid Email or You are Logged In\ud83d\ude1e";
			putRecentAuthFeedbackMessage(telegramId, textMsg);
			try { Thread.sleep(2000);} catch (InterruptedException e) { throw new RuntimeException(e);}
			deleteRecentAuthFeedbackMessage(telegramId);
			return;
		}

		Account userAccount = Account.signUp(email, password, telegramId, first_name, last_name, middle_names, UserType.CUSTOMER.getText(), null);
		userSessions.put(telegramId, userAccount);
		deleteRecentAuthFeedbackMessage(telegramId);
		String feedbackMsg = SignEmoji.GREEN_CIRCLE.getCode() + " You've Created Your Account Successfully \ud83d\udc4b";
		putRecentAuthFeedbackMessage(telegramId, feedbackMsg);
		try { Thread.sleep(2000);} catch (InterruptedException e) { throw new RuntimeException(e);}
		deleteRecentAuthFeedbackMessage(telegramId);

		Integer msgId = ((Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_ING_UP_MENU.getStep())).getMessageId();
		communicator.deleteMessage(telegramId, msgId);
		// task: show the dashboard
	}

	public static void signOut(Long telegramId) {
		Account userAccount = userSessions.get(telegramId);
		if (userAccount == null) {
			String msg = SignEmoji.ORANGE_CIRCLE.getCode() + " You are not logged in\\!";
			putRecentAuthFeedbackMessage(telegramId, msg);
			try { Thread.sleep(2000);} catch (InterruptedException e) { throw new RuntimeException(e);}
			deleteRecentAuthFeedbackMessage(telegramId);
			return;
		}

		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSessions.remove(telegramId);
		if (QuickBite.sessionState.get(telegramId) != null) QuickBite.sessionState.get(telegramId).clear();
		String msg = SignEmoji.GREEN_CIRCLE.getCode() + " *_You have log out successfully\\. See you soon\\! \ud83d\udc4b_*";
		putRecentAuthFeedbackMessage(telegramId, msg);
		try { Thread.sleep(2000);} catch (InterruptedException e) { throw new RuntimeException(e);}
		deleteRecentAuthFeedbackMessage(telegramId);
	}

	private static void putRecentAuthFeedbackMessage(Long telegramId, String textMessage) {
		Message fm = communicator.sendText(telegramId, textMessage);
		HashMap<String, Object> fmStep = new HashMap<>();
		fmStep.put(AuthSteps.SIGN_ING_UP_FEEDBACK_MSG.getStep(), fm);
		authProcesses.put(telegramId, fmStep);
	}

	private static void deleteRecentAuthFeedbackMessage(Long telegramId) {
		if (Authentication.authProcesses.get(telegramId).get(AuthSteps.SIGN_ING_UP_FEEDBACK_MSG.getStep()) == null) return;
		Message msg = (Message) Authentication.authProcesses.get(telegramId).get(AuthSteps.SIGN_ING_UP_FEEDBACK_MSG.getStep());
		communicator.deleteMessage(telegramId, msg.getMessageId());
	}
}
