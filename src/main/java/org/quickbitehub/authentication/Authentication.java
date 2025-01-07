package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.quickbitehub.app.State;
import org.quickbitehub.app.UserState;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.communicator.PageFactory;
import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.consumer.UserType;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.Objects;

public class Authentication {
	static final HashMap<Long, HashMap<String, Object>> authProcesses = new HashMap<>(); // device(Telegram Account Id) -> Current Step
	public static final HashMap<Long, Account> userSessions = new HashMap<>(); // TelegramId -> Account

	// info source: https://www.baeldung.com/java-email-validation-regex
	static public boolean isEmailValid(String email) {
		if (email == null || email.isBlank()) return false;
		email = email.strip().trim().toLowerCase();

		String gmailPattern = "^(?=.{1,64}@)[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*@"
					+ "[^-][A-Za-z0-9+-]+(\\.[A-Za-z0-9+-]+)*(\\.[A-Za-z]{2,})$";

		String nonLatinPattern = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
					+ "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.\\p{L}{2,})$";

		return email.matches(nonLatinPattern) ||
				email.matches(gmailPattern) ||
				EmailValidator.getInstance().isValid(email);
	}

	private static boolean isAuthNeeded(Long telegramId) {
		if (!isSessionAuthenticated(telegramId)) return true;
		String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed in\\!";
		MessageHandler.sendShortNotice(telegramId, msg);
		State.popAuthRelatedState(telegramId);
		return false;
	}

	public static Integer authenticate(Long telegramId) {
		if (!isAuthNeeded(telegramId)) return null;
		Message authMsg = PageFactory.viewAuthenticationPage(telegramId);
		HashMap<String, Object> menuStep = new HashMap<>();
		menuStep.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), authMsg);
		authProcesses.put(telegramId, menuStep);
		return authMsg.getMessageId();
	}

	private static void getReplySendNextPrompt(Message message, Long telegramId, HashMap<String, Object> userAuthSteps, String msg, String txt, String nextMsgKey, String nextMsgPrompt) {
		if (userAuthSteps == null || userAuthSteps.get(msg) == null) {
			Message emailMsg = MessageHandler.sendForceReply(telegramId, "Enter Email\\:", TimeConstants.STANDARD_DELAY_TIME_SEC.time());

			HashMap<String, Object> existingProcess = authProcesses.getOrDefault(telegramId, new HashMap<>());
			existingProcess.put(nextMsgKey, emailMsg);
			authProcesses.put(telegramId, existingProcess);
			State.pushRequiredState(telegramId, Pair.of(UserState.AUTHENTICATION_SIGNIN, null));
			return;
		}
		Message enteredEmail = (Message) userAuthSteps.get(msg);
		if (!Objects.equals(message.getReplyToMessage().getMessageId(), enteredEmail.getMessageId())) return;

		userAuthSteps.put(txt, message.getText().trim().strip());
		userAuthSteps.remove(msg);
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.NO_DELAY_TIME.time());

		Message nextMessage = MessageHandler.sendForceReply(telegramId, nextMsgPrompt, TimeConstants.STANDARD_DELAY_TIME_SEC.time());
		userAuthSteps.put(nextMsgKey, nextMessage);
		authProcesses.put(telegramId, userAuthSteps);
	}

	public static void signIn(Message message, Long telegramId) {
		if (!isAuthNeeded(telegramId)) return;

		var userAuthSteps = authProcesses.get(telegramId);
		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				null,
				null,
				AuthSteps.SIGNING_EMAIL_MSG.getStep(),
				"Enter Email\\:"
		);

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
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		String email = ((String) userAuthSteps.get(AuthSteps.SIGNING_EMAIL_TXT.getStep())).strip().trim().toLowerCase();

		signInHandler(telegramId, email, password);
	}

	private static void signInHandler(Long telegramId, String email, String password) {
		if (isAuthInfoInvalid(telegramId, email, password, UserState.AUTHENTICATION_SIGNIN)) return;
		email = Account.formatEmail(email);
		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			String textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
			MessageHandler.sendShortNotice(telegramId, textMsg);

			var menuMsg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
			HashMap<String, Object> temp = new HashMap<>();
			temp.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), menuMsg);
			authProcesses.remove(telegramId); // the process is finished
			authProcesses.put(telegramId, temp);
			State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
			return;
		}

		userSessions.put(telegramId, userAccount);
		Integer msgId = ((Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep())).getMessageId();
		MessageHandler.deleteMessage(telegramId, msgId, TimeConstants.NO_DELAY_TIME.time());

		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You Signed In Successfully " + Emoji.HAND_WAVING.getCode();
		MessageHandler.sendShortNotice(telegramId, feedbackMsg);
		authProcesses.remove(telegramId); // the process is finished
		State.popAuthRelatedState(telegramId);
	}

	public static void signUp(Message message, Long telegramId) {
		if (!isAuthNeeded(telegramId)) return;

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				null,
				null,
				AuthSteps.SIGNUP_EMAIL_MSG.getStep(),
				"Enter Email\\:"
		);

		getReplySendNextPrompt(message, telegramId, authProcesses.get(telegramId),
				AuthSteps.SIGNUP_EMAIL_MSG.getStep(),
				AuthSteps.SIGNUP_EMAIL_TXT.getStep(),
				AuthSteps.SIGNUP_PASSWORD_MSG.getStep(),
				"Enter Password\\:"
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
		MessageHandler.deleteMessage(telegramId, message.getReplyToMessage().getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		MessageHandler.deleteMessage(telegramId, message.getMessageId(), TimeConstants.NO_DELAY_TIME.time());
		if (middleNames.length() == 1) middleNames = "";

		String email = (String) userAuthSteps.get(AuthSteps.SIGNUP_EMAIL_TXT.getStep());
		String password = (String) userAuthSteps.get(AuthSteps.SIGNUP_PASSWORD_TXT.getStep());
		String firstName = (String) userAuthSteps.get(AuthSteps.SIGNUP_FIRST_NAME_TXT.getStep());
		String lastName = (String) userAuthSteps.get(AuthSteps.SIGNUP_LAST_NAME_TXT.getStep());

		signUpHandler(telegramId, email, password, firstName, lastName, middleNames);
	}

	private static void signUpHandler(Long telegramId, String email, String password, String firstName, String lastName, String middleNames) {
		if (isAuthInfoInvalid(telegramId, email, password, UserState.AUTHENTICATION_SIGNUP)) return;
		if (!isAccountInformationValid(telegramId, firstName, lastName, middleNames)) return;
		String unformattedEmail = email;
		email = Account.formatEmail(email);
		firstName = Account.formatName(firstName);
		lastName = Account.formatName(lastName);
		if (!middleNames.isBlank()) middleNames = Account.formatName(middleNames);

		Account userAccount = Account.signUp(email, unformattedEmail, password, telegramId, firstName, lastName, middleNames, UserType.CUSTOMER.getText(), null);
		userSessions.put(telegramId, userAccount);
		Message msg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
		MessageHandler.deleteMessage(telegramId, msg.getMessageId(), TimeConstants.NO_DELAY_TIME.time());

		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You've Created Your Account Successfully " + Emoji.HAND_WAVING.getCode();
		MessageHandler.sendShortNotice(telegramId, feedbackMsg);
		authProcesses.remove(telegramId); // the process is finished
		State.popAuthRelatedState(telegramId);
	}
	static boolean isAccountInformationValid(Long telegramId, String firstName, String lastName, String middleNames) {
		firstName = firstName.trim().strip();
		lastName = lastName.trim().strip();
		middleNames = middleNames.trim().strip();
		String fullName = firstName + " " + lastName + " " + middleNames;
		// is fullName contains only Unicode letters and spaces
		if (-1 == firstName.indexOf(' ') && -1 == lastName.indexOf(' ') && fullName.matches("^[\\p{L} ]+$")) return true;

		String textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign Up Failed*\nInvalid First/Last/Middle Name\\(s\\) " + Emoji.SAD_FACE.getCode();
		MessageHandler.sendShortNotice(telegramId, textMsg);

		var menuMsg = (Message) Authentication.authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
		HashMap<String, Object> temp = new HashMap<>();
		temp.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), menuMsg);
		Authentication.authProcesses.remove(telegramId); // the process is finished
		Authentication.authProcesses.put(telegramId, temp);
		State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
		return false;
	}
	private static boolean isAuthInfoInvalid(Long telegramId, String email, String password, UserState processType) {
		if (processType == UserState.AUTHENTICATION_SIGNIN) {
			if (isEmailValid(email) &&
					Account.isAccountExist(Account.formatEmail(email)) &&
					userSessions.get(telegramId) == null) {
				Account userAccount = Account.authenticate(telegramId, Account.formatEmail(email), password);
				return userAccount == null;
			}
		} else {
			if (isEmailValid(email) &&
					!Account.isAccountExist(Account.formatEmail(email)) &&
					userSessions.get(telegramId) == null) {
				return false;
			}
		}

		String textMsg = "";
		if (userSessions.get(telegramId) != null) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign In/Up Failed*\nYou are already signed in " + Emoji.SAD_FACE.getCode();
		} else if (!isEmailValid(email)) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In/Up Failed*\nInvalid Email " + Emoji.SAD_FACE.getCode();
		} else if (Account.isAccountExist(Account.formatEmail(email))) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign Up Failed*\nYou Already Have an Account; Just Sign In " + Emoji.SMILING_FACE.getCode();
		} else if (Account.authenticate(telegramId, Account.formatEmail(email), password) == null) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
		}
		MessageHandler.sendShortNotice(telegramId, textMsg);

		var menuMsg = (Message) authProcesses.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
		HashMap<String, Object> temp = new HashMap<>();
		temp.put(AuthSteps.SIGN_IN_UP_MENU.getStep(), menuMsg);
		authProcesses.remove(telegramId); // the process is finished
		authProcesses.put(telegramId, temp);
		State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
		return true;
	}

	public static void signOut(Long telegramId) {
		Account userAccount = userSessions.get(telegramId);
		if (userAccount == null) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed out\\!";
			MessageHandler.sendShortNotice(telegramId, msg);
			return;
		}
		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSessions.remove(telegramId);
		String msg = Emoji.GREEN_CIRCLE.getCode() + " *_You have log out successfully\\. See you soon\\! \ud83d\udc4b_*";
		MessageHandler.sendShortNotice(telegramId, msg);
	}

	public static boolean isSessionAuthenticated(Long telegramId) {
		return userSessions.get(telegramId) != null;
	}
	public static Account getSessionAccount(Long telegramId) {
		return userSessions.get(telegramId);
	}
}
