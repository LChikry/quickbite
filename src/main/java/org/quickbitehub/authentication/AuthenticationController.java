package org.quickbitehub.authentication;

import org.quickbitehub.account.Account;
import org.quickbitehub.authentication.signin.SignInService;
import org.quickbitehub.authentication.signup.SignUpService;
import org.quickbitehub.state.UserState;
import org.quickbitehub.communicator.*;

public class AuthenticationController {
	private final AuthenticationService authService;
	private final SignInService signInService;
	private final SignUpService signUpService;
	private static AuthenticationController instance;

	private AuthenticationController(AuthenticationService authService) {
		this.authService = authService;
		this.signInService = SignInService.INSTANCE;
		this.signUpService = SignUpService.INSTANCE;
	}
	/**
	 * @return if it is existed, it returns single instance of AuthenticationController. Otherwise, it returns null which indicates the need to call initInstance function.
	 */
	public static AuthenticationController getInstance() {
		if (instance != null) return instance;
		synchronized (AuthenticationController.class) {
			if (instance != null) return instance;
			return null;
		}
	}
	/**
	 * @param authService inject AuthenticationService instance
	 * @return if it is not instantiated yet, it returns a new single instance of AuthenticationController. Otherwise, it returns the existed instance
	 */
	public static AuthenticationController initInstance(AuthenticationService authService) {
		if (instance != null) return instance;
		synchronized (AuthenticationController.class) {
			if (instance != null) return instance;
			instance = new AuthenticationController(authService);
			return instance;
		}
	}

	public boolean isChatAuthenticated(Long chatId) {
		return authService.isChatAuthenticated(chatId);
	}
	public Account getChatAccount(Long chatId) {
		return authService.getChatAccount(chatId);
	}
	public String getAuthStateValue(Long chatId, UserState authState) {
		return authService.getAuthStateValue(chatId, authState);
	}

	public Integer viewAuthenticationPage(Long chatId, Integer messageId) {
		if (isChatAuthenticated(chatId)) {
			authService.respondToUnnecessaryAuthRequest(chatId);
			return null;
		}
		return PageFactory.viewAuthenticationPage(chatId, messageId);
	}

	public void viewSignInPage(Long chatId, Integer messageId) {
		if (isChatAuthenticated(chatId)) {
			authService.respondToUnnecessaryAuthRequest(chatId);
			return;
		}
		authService.initChatAuthState(chatId);
		signInService.viewSignInPage(chatId, messageId);
	}
	public void processSignIn(Long chatId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
		if (!authService.isChatHasAuthState(chatId)) {
			authService.handleExpiredAuthForms(instance, chatId, answerMessageId, UserState.SIGNIN_PAGE);
			return;
		}
		switch (authState) {
			case __SET_SIGNIN_EMAIL, __SET_SIGNIN_PASSWORD -> authService.askForCredentials(chatId, authState);
			case __GET_SIGNIN_EMAIL, __GET_SIGNIN_PASSWORD -> {
				signInService.collectSignInCredentials(chatId, authState, answerMessageId, promptMessageId, credential);
			}
			case __CONFIRM_SIGNIN -> signInService.confirmSignIn(chatId);
		}
	}

	public void viewSignUpPage(Long chatId, Integer messageId) {
		if (isChatAuthenticated(chatId)) {
			authService.respondToUnnecessaryAuthRequest(chatId);
			return;
		}
		authService.initChatAuthState(chatId);
		signUpService.viewSignUpPage(chatId, messageId);
	}
	public void processSignUp(Long chatId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
		if (!authService.isChatHasAuthState(chatId)) {
			authService.handleExpiredAuthForms(instance, chatId, answerMessageId, UserState.SIGNUP_PAGE);
			return;
		}
		switch (authState) {
			case __SET_SIGNUP_EMAIL, __SET_SIGNUP_PASSWORD, __SET_SIGNUP_FIRST_NAME, __SET_SIGNUP_LAST_NAME,
			     __SET_SIGNUP_MIDDLE_NAMES -> authService.askForCredentials(chatId, authState);
			case __GET_SIGNUP_EMAIL, __GET_SIGNUP_PASSWORD, __GET_SIGNUP_FIRST_NAME,
			     __GET_SIGNUP_MIDDLE_NAMES, __GET_SIGNUP_LAST_NAME -> {
				signUpService.collectSignUpCredentials(chatId, authState, answerMessageId, promptMessageId, credential);
			}
			case __CONFIRM_SIGNUP -> signUpService.confirmSignUp(chatId);
		}
	}

	public void signOut(Long chatId) {
		authService.handleSignOut(chatId);
		viewAuthenticationPage(chatId, null);
	}
}