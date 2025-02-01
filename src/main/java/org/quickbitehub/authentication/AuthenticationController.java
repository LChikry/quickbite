package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.quickbitehub.account.Account;
import org.quickbitehub.app.State;
import org.quickbitehub.app.UserState;
import org.quickbitehub.communicator.*;
import org.quickbitehub.consumer.Customer;

import java.util.HashMap;

public class AuthenticationController {
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
	private static boolean isAuthInformationValid(Long telegramId, UserState authState, String credential) {
		if (authState == UserState.__GET_SIGNIN_EMAIL && !isEmailValid(credential)) {
			MessageHandler.sendShortNotice(telegramId, Emoji.RED_CIRCLE.getCode() + " Invalid Email");
			State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNIN_EMAIL, null));
			return false;
		}
		if (authState == UserState.__GET_SIGNUP_EMAIL && (!isEmailValid(credential) || Account.isAccountExist(credential))) {
			String errorMsg = null;
			if (!isEmailValid(credential)) 	errorMsg = " Invalid Email Address";
			else errorMsg = " This Email Is Already Linked to Another Account";
			MessageHandler.sendShortNotice(telegramId, Emoji.RED_CIRCLE.getCode() + errorMsg);
			State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNUP_EMAIL, null));
			return false;
		}
		if ((authState == UserState.__GET_SIGNUP_FIRST_NAME || authState == UserState.__GET_SIGNUP_LAST_NAME) && !isSingleNameValid(credential)) {
			MessageHandler.sendShortNotice(telegramId, Emoji.RED_CIRCLE.getCode() + " Invalid Name");
			if (authState == UserState.__GET_SIGNUP_FIRST_NAME) State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNUP_FIRST_NAME, null));
			else State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNUP_LAST_NAME, null));
			return false;
		}
		if (authState == UserState.__GET_SIGNUP_MIDDLE_NAMES && !isMultiNamesValid(credential)) {
			MessageHandler.sendShortNotice(telegramId, Emoji.RED_CIRCLE.getCode() + " Invalid Middle Name(s)");
			State.applyImmediateState(telegramId, Pair.of(UserState.__SET_SIGNUP_MIDDLE_NAMES, null));
			return false;
		}
		return true;
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
		UserState oppositeState = authState.getOppositeAuthState();
		MessageHandler.deleteMessage(telegramId, Integer.valueOf(userAuthState.get(oppositeState)), TimeConstants.NO_TIME.time());
		MessageHandler.deleteMessage(telegramId, answerMessageId, TimeConstants.NO_TIME.time());
		if (!isAuthInformationValid(telegramId, authState, credential)) return;

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
		authenticationState.remove(telegramId);
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
		State.popAuthRelatedState(telegramId);
	}
	public static void signIn(Long telegramId, UserState signInState, Integer messageId, Integer olderMessageId, String credential) {
		if (!isAuthNeeded(telegramId)) return;
		authenticationState.putIfAbsent(telegramId, new HashMap<>());

		switch (signInState) {
			case SIGNIN_PAGE -> viewSignInPage(telegramId, messageId);
			case __SET_SIGNIN_EMAIL, __SET_SIGNIN_PASSWORD -> askForCredentials(telegramId, signInState);
			case __GET_SIGNIN_EMAIL, __GET_SIGNIN_PASSWORD -> collectSignInCredentials(telegramId, signInState, messageId, olderMessageId, credential);
			case __CONFIRM_SIGNIN -> confirmSignIn(telegramId);
			default -> {assert false;}
		}
	}

	public static void signUp(Long telegramId, UserState signUpState, Integer messageId, Integer olderMessageId, String credential) {
		if (!isAuthNeeded(telegramId)) return;
		authenticationState.putIfAbsent(telegramId, new HashMap<>());

		switch (signUpState) {
			case SIGNUP_PAGE -> viewSignUpPage(telegramId, messageId);
			case __SET_SIGNUP_EMAIL, __SET_SIGNUP_PASSWORD, __SET_SIGNUP_FIRST_NAME, __SET_SIGNUP_LAST_NAME, __SET_SIGNUP_MIDDLE_NAMES -> askForCredentials(telegramId, signUpState);
			case __GET_SIGNUP_EMAIL, __GET_SIGNUP_PASSWORD, __GET_SIGNUP_FIRST_NAME, __GET_SIGNUP_MIDDLE_NAMES, __GET_SIGNUP_LAST_NAME -> collectSignUpCredentials(telegramId, signUpState, messageId, olderMessageId, credential);
			case __CONFIRM_SIGNUP -> confirmSignUp(telegramId);
			default -> {assert false;}
		}
	}
	private static void viewSignUpPage(long telegramId, Integer messageId) {
		assert (messageId != null);
		var userAuthState = authenticationState.get(telegramId);
		userAuthState.clear();
		var kbId = PageFactory.viewSignUpPage(telegramId, messageId, null, null, null, null, null);
		if (kbId == null) kbId = messageId;
		userAuthState.put(UserState.SIGNUP_PAGE, String.valueOf(kbId.intValue()));
		if (!kbId.equals(messageId)) State.updateKeyboardState(telegramId, kbId, UserState.SIGNUP_PAGE);
	}
	private static void collectSignUpCredentials(long telegramId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
		assert (answerMessageId != null && promptMessageId != null && credential != null);
		var userAuthState = authenticationState.get(telegramId);
		UserState oppositeState = authState.getOppositeAuthState();
		MessageHandler.deleteMessage(telegramId, Integer.valueOf(userAuthState.get(oppositeState)), TimeConstants.NO_TIME.time());
		MessageHandler.deleteMessage(telegramId, answerMessageId, TimeConstants.NO_TIME.time());
		if (!isAuthInformationValid(telegramId, authState, credential)) return;

		userAuthState.put(authState, credential);
		String email = userAuthState.get(UserState.__GET_SIGNUP_EMAIL);
		String password = null;
		if (userAuthState.get(UserState.__GET_SIGNUP_PASSWORD) != null) password = Emoji.PASSWORD_DOT.getCode().repeat(10);
		String firstName = userAuthState.get(UserState.__GET_SIGNUP_FIRST_NAME);
		firstName = Account.formatName(firstName);
		String lastName = userAuthState.get(UserState.__GET_SIGNUP_LAST_NAME);
		lastName = Account.formatName(lastName);
		String middleNames = userAuthState.get(UserState.__GET_SIGNUP_MIDDLE_NAMES);
		middleNames = Account.formatName(middleNames);

		PageFactory.updateSignUpPage(telegramId, Integer.valueOf(userAuthState.get(UserState.SIGNUP_PAGE)), email, password, firstName, lastName, middleNames);
	}
	private static boolean isSingleNameValid(String name) {
		name = name.trim().strip();
		return -1 == name.indexOf(' ') && isMultiNamesValid(name);
	}
	private static boolean isMultiNamesValid(String multiNames) {
		multiNames = multiNames.trim().strip();
		// is name contains only Unicode letters and spaces
		return multiNames.matches("^[\\p{L} ]+$");
	}
	public static void confirmSignUp(long telegramId) {
		var userAuthState = authenticationState.get(telegramId);
		String unformattedEmail = userAuthState.get(UserState.__GET_SIGNUP_EMAIL);
		String email = Account.formatEmail(unformattedEmail);
		String password = userAuthState.get(UserState.__GET_SIGNUP_PASSWORD);
		String firstName = userAuthState.get(UserState.__GET_SIGNUP_FIRST_NAME);
		firstName = Account.formatName(firstName);
		String lastName = userAuthState.get(UserState.__GET_SIGNUP_LAST_NAME);
		lastName = Account.formatName(lastName);
		String middleNames = userAuthState.get(UserState.__GET_SIGNUP_MIDDLE_NAMES);
		if (!middleNames.isBlank()) middleNames = Account.formatName(middleNames);

		authenticationState.remove(telegramId);
		Customer user = new Customer(firstName, lastName, middleNames);
//		Account userAccount = Account.signUp(email, unformattedEmail, password, telegramId, firstName, lastName, middleNames, UserType.CUSTOMER.getText(), null);
		Account userAccount = new Account(email, unformattedEmail, password, user, telegramId);
		userSession.put(telegramId, userAccount);
		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You Have Created Your Account Successfully " + Emoji.HAND_WAVING.getCode();
		MessageHandler.sendShortNotice(telegramId, feedbackMsg);
		State.popAuthRelatedState(telegramId);
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
