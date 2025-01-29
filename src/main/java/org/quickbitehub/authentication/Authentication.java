package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.quickbitehub.app.State;
import org.quickbitehub.app.UserState;
import org.quickbitehub.communicator.*;

import java.util.HashMap;

public class Authentication {
	static final HashMap<Long, HashMap<UserState, String>> authenticationState = new HashMap<>(); // device(telegram id) -> Auth Steps -> related message id
	public static final HashMap<Long, Account> userSession = new HashMap<>(); // TelegramId -> Account

	public static boolean isSessionAuthenticated(Long telegramId) { return userSession.get(telegramId) != null; }
	public static Account getSessionAccount(Long telegramId) { return userSession.get(telegramId); }
	public static String getAuthStepValue(Long telegramId, UserState authStep) { return authenticationState.get(telegramId).get(authStep);}

	// info source: https://www.baeldung.com/java-email-validation-regex
	public static boolean isEmailValid(String email) {
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
	private static void askForCredentials(long telegramId, UserState authState) {
		String authPrompt = switch (authState) {
			case __SET_SIGNIN_EMAIL, __SET_SIGNUP_EMAIL -> AuthPrompts.EMAIL.getPrompt();
			case __SET_SIGNIN_PASSWORD, __SET_SIGNUP_PASSWORD -> AuthPrompts.PASSWORD.getPrompt();
			case __SET_SIGNUP_FIRST_NAME -> AuthPrompts.FIRST_NAME.getPrompt();
			case __SET_SIGNUP_LAST_NAME -> AuthPrompts.LAST_NAME.getPrompt();
			case __SET_SIGNUP_MIDDLE_NAMES -> AuthPrompts.MIDDLE_NAMES.getPrompt();
			default -> null;
		};
		assert (authPrompt != null);

		var msgId = MessageHandler.sendForceReply(telegramId, authPrompt, TimeConstants.NO_TIME.time());
		assert (msgId != null);
		State.addKeyboardRelatedMessage(telegramId, msgId);
		var userAuthState = authenticationState.get(telegramId);
		userAuthState.put(authState, String.valueOf(msgId.intValue()));
	}

	public static Integer authenticate(Long telegramId, Integer messageId) {
		if (!isAuthNeeded(telegramId)) return null;
		return PageFactory.viewAuthenticationPage(telegramId, messageId);
	}

	private static void viewSignInPage(long telegramId, Integer messageId) {
		assert (messageId != null);
		var userAuthState = authenticationState.get(telegramId);
		userAuthState.clear();
		var kbId = PageFactory.viewSignInPage(telegramId, messageId, null, null);
		if (kbId == null) kbId = messageId;
		userAuthState.put(UserState.SIGNIN_PAGE, String.valueOf(kbId.intValue()));
		if (!kbId.equals(messageId)) State.updateKeyboardState(telegramId, kbId, UserState.SIGNIN_PAGE);
	}
	private static void collectSignInCredentials(long telegramId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
		assert (answerMessageId != null && promptMessageId != null && credential != null);
		var userAuthState = authenticationState.get(telegramId);
		UserState promptMessageState = null;
		if (authState == UserState.__GET_SIGNIN_PASSWORD) {
			promptMessageState = UserState.__SET_SIGNIN_PASSWORD;
			MessageHandler.deleteMessage(telegramId, Integer.valueOf(userAuthState.get(UserState.__SET_SIGNIN_PASSWORD)), TimeConstants.NO_TIME.time());
			MessageHandler.deleteMessage(telegramId, answerMessageId, TimeConstants.NO_TIME.time());
		} else if (authState == UserState.__GET_SIGNIN_EMAIL) {
			promptMessageState = UserState.__SET_SIGNIN_EMAIL;
			State.addKeyboardRelatedMessage(telegramId, answerMessageId);
		}
		else assert (false);

		System.out.println("answer: " + answerMessageId);
		System.out.println("prompt: " + promptMessageId);
		if (!String.valueOf(promptMessageId.intValue()).equals(userAuthState.get(promptMessageState))) {
			MessageFactory.sendIncorrectInputNotice(telegramId);
			return;
		}

		if (authState == UserState.__GET_SIGNIN_EMAIL && !isEmailValid(credential)) {
			MessageHandler.sendShortNotice(telegramId, Emoji.RED_CIRCLE.getCode() + " Invalid Email");
			State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNIN_EMAIL, null));
			return;
		}

		userAuthState.put(authState, credential);
		String email = userAuthState.get(UserState.__GET_SIGNIN_EMAIL);
		String password = null;
		if (userAuthState.get(UserState.__GET_SIGNIN_PASSWORD) != null) password = Emoji.PASSWORD_DOT.getCode().repeat(10);
		PageFactory.updateSignInPage(telegramId, Integer.valueOf(userAuthState.get(UserState.SIGNIN_PAGE)), email, password);
	}
	public static void confirmSignIn(long telegramId) {
		var userAuthState = authenticationState.get(telegramId);
		String email = userAuthState.get(UserState.__GET_SIGNIN_EMAIL);
		email = Account.formatEmail(email);
		String password = userAuthState.get(UserState.__GET_SIGNIN_PASSWORD);
		userAuthState.clear();
		signInHandler(telegramId, email, password);
	}
	public static void signIn(Long telegramId, UserState signInState, Integer messageId, Integer olderMessageId, String credential) {
		if (!isAuthNeeded(telegramId)) return;
		if (!authenticationState.containsKey(telegramId)) authenticationState.put(telegramId, new HashMap<>());

		switch (signInState) {
			case SIGNIN_PAGE -> viewSignInPage(telegramId, messageId);
			case __SET_SIGNIN_EMAIL, __SET_SIGNIN_PASSWORD -> askForCredentials(telegramId, signInState);
			case __GET_SIGNIN_EMAIL, __GET_SIGNIN_PASSWORD -> collectSignInCredentials(telegramId, signInState, messageId, olderMessageId, credential);
			case __CONFIRM_SIGNIN -> confirmSignIn(telegramId);
			default -> {assert false;}
		}
	}
	private static void signInHandler(Long telegramId, String email, String password) {
		if (isAuthInfoInvalid(telegramId, email, password, UserState.SIGNIN_PAGE)) return;
		Account userAccount = Account.authenticate(telegramId, email, password);
		if (userAccount == null) {
			String textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
			MessageHandler.sendShortNotice(telegramId, textMsg);
			State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
			return;
		}

		userSession.put(telegramId, userAccount);
		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You Signed In Successfully " + Emoji.HAND_WAVING.getCode();
		MessageHandler.sendShortNotice(telegramId, feedbackMsg);
		authenticationState.remove(telegramId); // the process is finished
		State.popAuthRelatedState(telegramId);
	}
//	private static void signUpHandler(Long telegramId, String email, String password, String firstName, String lastName, String middleNames) {
//		if (isAuthInfoInvalid(telegramId, email, password, UserState.SIGNUP_PAGE)) return;
//		if (!isAccountInformationValid(telegramId, firstName, lastName, middleNames)) return;
//		String unformattedEmail = email;
//		email = Account.formatEmail(email);
//		firstName = Account.formatName(firstName);
//		lastName = Account.formatName(lastName);
//		if (!middleNames.isBlank()) middleNames = Account.formatName(middleNames);
//
//		Account userAccount = Account.signUp(email, unformattedEmail, password, telegramId, firstName, lastName, middleNames, UserType.CUSTOMER.getText(), null);
//		userSession.put(telegramId, userAccount);
//		Message msg = (Message) authenticationState.get(telegramId).get(AuthSteps.SIGN_IN_UP_MENU.getStep());
//		MessageHandler.deleteMessage(telegramId, msg.getMessageId(), TimeConstants.NO_TIME.time());
//
//		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You've Created Your Account Successfully " + Emoji.HAND_WAVING.getCode();
//		MessageHandler.sendShortNotice(telegramId, feedbackMsg);
//		authenticationState.remove(telegramId); // the process is finished
//		State.popAuthRelatedState(telegramId);
//	}
	static boolean isAccountInformationValid(Long telegramId, String firstName, String lastName, String middleNames) {
		firstName = firstName.trim().strip();
		lastName = lastName.trim().strip();
		middleNames = middleNames.trim().strip();
		String fullName = firstName + " " + lastName + " " + middleNames;
		// is fullName contains only Unicode letters and spaces
		if (-1 == firstName.indexOf(' ') && -1 == lastName.indexOf(' ') && fullName.matches("^[\\p{L} ]+$")) return true;

		String textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign Up Failed*\nInvalid First/Last/Middle Name\\(s\\) " + Emoji.SAD_FACE.getCode();
		MessageHandler.sendShortNotice(telegramId, textMsg);
		State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
		return false;
	}
	private static boolean isAuthInfoInvalid(Long telegramId, String email, String password, UserState processType) {
		if (processType == UserState.SIGNIN_PAGE) {
			if (isEmailValid(email) &&
					Account.isAccountExist(Account.formatEmail(email)) &&
					userSession.get(telegramId) == null) {
				Account userAccount = Account.authenticate(telegramId, Account.formatEmail(email), password);
				return userAccount == null;
			}
		} else {
			if (isEmailValid(email) &&
					!Account.isAccountExist(Account.formatEmail(email)) &&
					userSession.get(telegramId) == null) {
				return false;
			}
		}

		String textMsg = "";
		if (userSession.get(telegramId) != null) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign In/Up Failed*\nYou are already signed in " + Emoji.SAD_FACE.getCode();
		} else if (!isEmailValid(email)) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In/Up Failed*\nInvalid Email " + Emoji.SAD_FACE.getCode();
		} else if (Account.isAccountExist(Account.formatEmail(email))) {
			textMsg = Emoji.ORANGE_CIRCLE.getCode() + " *Sign Up Failed*\nYou Already Have an Account; Just Sign In " + Emoji.SMILING_FACE.getCode();
		} else if (Account.authenticate(telegramId, Account.formatEmail(email), password) == null) {
			textMsg = Emoji.RED_CIRCLE.getCode() + " *Sign In Failed*\nIncorrect Email or Password " + Emoji.SAD_FACE.getCode();
		}
		MessageHandler.sendShortNotice(telegramId, textMsg);
		State.applyImmediateState(telegramId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
		return true;
	}
	public static void signOut(Long telegramId) {
		Account userAccount = userSession.get(telegramId);
		if (userAccount == null) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " You are already signed out\\!";
			MessageHandler.sendShortNotice(telegramId, msg);
			return;
		}
		assert userAccount.isAuthenticated(telegramId);
		userAccount.logOut(telegramId);
		userSession.remove(telegramId);
		String msg = Emoji.GREEN_CIRCLE.getCode() + " *_You have log out successfully\\. See you soon\\! \ud83d\udc4b_*";
		MessageHandler.sendShortNotice(telegramId, msg);
	}
}
