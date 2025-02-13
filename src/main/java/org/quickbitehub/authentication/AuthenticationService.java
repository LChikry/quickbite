package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.quickbitehub.account.Account;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.TimeConstants;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
	private final Map<Long, Map<UserState, String>> authenticationState; // device(telegram id) -> Auth Steps -> related message id
	private final Map<Long, Account> authenticatedChats; // TelegramId -> Account
	private static AuthenticationService instance;

	private AuthenticationService(Map<Long, Map<UserState, String>> authenticationState, Map<Long, Account> authenticatedChats){
		this.authenticationState = authenticationState;
		this.authenticatedChats = authenticatedChats;
	}

	public static AuthenticationService getInstance() {
		if (instance != null) return instance;
		synchronized (AuthenticationService.class) {
			if (instance != null) return instance;
			instance = new AuthenticationService(new HashMap<>(), new HashMap<>());
			return instance;
		}
	}

	public static AuthenticationService getInstance(Map<Long, Map<UserState, String>> authenticationState, Map<Long, Account> authenticatedChats) {
		if (instance != null) return instance;
		synchronized (AuthenticationService.class) {
			if (instance != null) return instance;
			instance = new AuthenticationService(authenticationState, authenticatedChats);
			return instance;
		}
	}

	void initChatAuthState(Long chatId) {
		authenticationState.putIfAbsent(chatId, new HashMap<>());
	}
	void handleExpiredAuthForms(AuthenticationController instance, Long chatId, Integer messageId, UserState authFormType) {
		MessageHandler.deleteMessage(chatId, messageId, TimeConstants.NO_TIME.time());
		if (authFormType == UserState.SIGNIN_PAGE) instance.viewSignInPage(chatId, null);
		else instance.viewSignUpPage(chatId, null);
	}
	void askForCredentials(Long chatId, UserState authState) {
		String authPrompt = switch (authState) {
			case __SET_SIGNIN_EMAIL, __SET_SIGNUP_EMAIL -> AuthMessages.EMAIL_PROMPT.getPrompt();
			case __SET_SIGNIN_PASSWORD, __SET_SIGNUP_PASSWORD -> AuthMessages.PASSWORD_PROMPT.getPrompt();
			case __SET_SIGNUP_FIRST_NAME -> AuthMessages.FIRST_NAME_PROMPT.getPrompt();
			case __SET_SIGNUP_LAST_NAME -> AuthMessages.LAST_NAME_PROMPT.getPrompt();
			case __SET_SIGNUP_MIDDLE_NAMES -> AuthMessages.MIDDLE_NAMES_PROMPT.getPrompt();
			default -> null;
		};
		assert (authPrompt != null);

		var msgId = MessageHandler.sendForceReply(chatId, authPrompt, TimeConstants.NO_TIME.time());
		assert (msgId != null);
		State.addKeyboardRelatedMessage(chatId, msgId);
		addAuthStateWithValue(chatId, authState, String.valueOf(msgId.intValue()));
	}
	void handleSignOut(Long chatId) {
		Account userAccount = getChatAccount(chatId);
		if (userAccount == null) {
			MessageHandler.sendShortNotice(chatId, AuthMessages.USER_ALREADY_SIGNED_OUT.getPrompt());
			return;
		}
		assert userAccount.isAuthenticated(chatId);
		userAccount.logOut(chatId);
		removeChatAccount(chatId);
		MessageHandler.sendShortNotice(chatId, AuthMessages.SUCCESSFUL_SIGNOUT.getPrompt());
	}

	/**
	 * @param authState authentication state (__GET_...) of the checked credential (email, password, etc.)
	 * @param credential checked credential value
	 * @return null if the credential is valid, otherwise, it returns the error message.
	 */
	String isUserCredentialFormatValid(UserState authState, String credential) {
		String errorMessage = null;
		switch (authState) {
			case __GET_SIGNIN_EMAIL -> {
				if (isEmailFormatInvalid(credential)) errorMessage = AuthMessages.INVALID_EMAIL.getPrompt();
				else if (!Account.isAccountExist(credential)) errorMessage = AuthMessages.EMAIL_DOES_NOT_EXIST.getPrompt();
			}
			case __GET_SIGNUP_EMAIL -> {
				if (isEmailFormatInvalid(credential)) errorMessage = AuthMessages.INVALID_EMAIL.getPrompt();
				else if (Account.isAccountExist(credential)) errorMessage = AuthMessages.EMAIL_ALREADY_EXIST.getPrompt();
			}
			case __GET_SIGNUP_FIRST_NAME, __GET_SIGNUP_LAST_NAME -> {
				if (!isSingleNameFormatValid(credential)) errorMessage = AuthMessages.INVALID_NAME.getPrompt();
			}
			case __GET_SIGNUP_MIDDLE_NAMES -> {
				if (!isMultiNamesFormatValid(credential)) errorMessage = AuthMessages.INVALID_MIDDLE_NAMES.getPrompt();
			}
		}
		return errorMessage;
	}
	void respondToInvalidCredentials(Long chatId, String errorMessage, UserState newState) {
		MessageHandler.sendShortNotice(chatId, Emoji.RED_CIRCLE.getCode() + " " + errorMessage);
		State.applyImmediateState(chatId, Pair.of(newState, null));
	}

	boolean isChatAuthenticated(Long chatId) {
		return authenticatedChats.get(chatId) != null;
	}
	void respondToUnnecessaryAuthRequest(Long chatId) {
		MessageHandler.sendShortNotice(chatId, AuthMessages.USER_ALREADY_SIGNED_IN.getPrompt());
		State.popAuthRelatedState(chatId);
	}

	Account getChatAccount(Long chatId) {
		return authenticatedChats.get(chatId);
	}
	void addChatAccount(Long chatId, Account userAccount) {
		authenticatedChats.put(chatId, userAccount);
	}
	boolean isChatHasAuthState(Long chatId) {
		return authenticationState.get(chatId) != null;
	}
	String getAuthStateValue(Long chatId, UserState authState) {
		return authenticationState.get(chatId).get(authState);
	}
	void addAuthStateWithValue(Long chatId, UserState authState, String value) {
		authenticationState.get(chatId).put(authState, value);
	}
	void clearChatAuthState(Long chatId) {
		authenticationState.get(chatId).clear();
	}
	void removeChatAuthState(Long chatId) {
		authenticationState.remove(chatId);
	}
	void removeChatAccount(Long chatId) {
		authenticatedChats.remove(chatId);
	}

	// info source: https://www.baeldung.com/java-email-validation-regex
	private boolean isEmailFormatInvalid(String email) {
		if (email == null || email.isBlank()) return true;
		email = email.strip().trim().toLowerCase();

		String gmailPattern = "^(?=.{1,64}@)[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*@"
				+ "[^-][A-Za-z0-9+-]+(\\.[A-Za-z0-9+-]+)*(\\.[A-Za-z]{2,})$";

		String nonLatinPattern = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
				+ "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.\\p{L}{2,})$";

		return !email.matches(nonLatinPattern) ||
				!email.matches(gmailPattern) ||
				!EmailValidator.getInstance().isValid(email);
	}
	private boolean isSingleNameFormatValid(String name) {
		name = name.trim().strip();
		return -1 == name.indexOf(' ') && isMultiNamesFormatValid(name);
	}
	private boolean isMultiNamesFormatValid(String multiNames) {
		multiNames = multiNames.trim().strip();
		// is name contains only Unicode letters and spaces
		return multiNames.matches("^[\\p{L} ]+$");
	}
}
