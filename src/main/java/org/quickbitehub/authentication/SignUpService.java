package org.quickbitehub.authentication;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.account.Account;
import org.quickbitehub.app.State;
import org.quickbitehub.app.UserState;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.PageFactory;
import org.quickbitehub.communicator.TimeConstants;
import org.quickbitehub.consumer.Customer;

enum SignUpService {
	INSTANCE;
	private final AuthenticationService authService = AuthenticationService.getInstance();

	void viewSignUpPage(Long chatId, Integer messageId) {
		authService.clearChatAuthState(chatId);
		var kbId = PageFactory.viewSignUpPage(chatId, messageId, null, null, null, null, null);
		if (kbId == null) kbId = messageId;
		authService.addAuthStateWithValue(chatId, UserState.SIGNUP_PAGE, String.valueOf(kbId.intValue()));
		if (!kbId.equals(messageId)) State.updateKeyboardState(chatId, kbId, UserState.SIGNUP_PAGE);
	}
	void collectSignUpCredentials(Long chatId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
		assert (answerMessageId != null && promptMessageId != null && credential != null);

		UserState oppositeState = authState.getOppositeAuthState();
		String stateValue = authService.getAuthStateValue(chatId, oppositeState);
		MessageHandler.deleteMessage(chatId, Integer.valueOf(stateValue), TimeConstants.NO_TIME.time());
		MessageHandler.deleteMessage(chatId, answerMessageId, TimeConstants.NO_TIME.time());

		String errorMessage = authService.isUserCredentialFormatValid(authState, credential);
		if (errorMessage != null) {
			authService.respondToInvalidCredentials(chatId, errorMessage, authState);
			return;
		}

		authService.addAuthStateWithValue(chatId, authState, credential);
		String email = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_EMAIL);
		String password = null;
		if (authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_PASSWORD) != null) {
			password = Emoji.PASSWORD_DOT.getCode().repeat(10);
		}
		String firstName = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_FIRST_NAME);
		firstName = Account.formatName(firstName);
		String lastName = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_LAST_NAME);
		lastName = Account.formatName(lastName);
		String middleNames = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_MIDDLE_NAMES);
		middleNames = Account.formatName(middleNames);

		Integer signUpPageId = Integer.valueOf(authService.getAuthStateValue(chatId, UserState.SIGNUP_PAGE));
		PageFactory.updateSignUpPage(chatId, signUpPageId, email, password, firstName, lastName, middleNames);
	}

	void confirmSignUp(Long chatId) {
		String unformattedEmail = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_EMAIL);
		String email = Account.formatEmail(unformattedEmail);
		String password = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_PASSWORD);
		String firstName = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_FIRST_NAME);
		firstName = Account.formatName(firstName);
		String lastName = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_LAST_NAME);
		lastName = Account.formatName(lastName);
		String middleNames = authService.getAuthStateValue(chatId, UserState.__GET_SIGNUP_MIDDLE_NAMES);
		middleNames = Account.formatName(middleNames);

		Customer user = new Customer(firstName, lastName, middleNames);
//		Account userAccount = Account.signUp(email, unformattedEmail, password, telegramId, firstName, lastName, middleNames, UserType.CUSTOMER.getText(), null);
		Account userAccount = new Account(email, unformattedEmail, password, user, chatId);
		authService.removeChatAuthState(chatId);
		authService.addChatAccount(chatId, userAccount);

		String feedbackMsg = Emoji.GREEN_CIRCLE.getCode() + " You Have Created Your Account Successfully " + Emoji.HAND_WAVING.getCode();
		MessageHandler.sendShortNotice(chatId, feedbackMsg);
		State.popAuthRelatedState(chatId);
		State.applyImmediateState(chatId, Pair.of(UserState.DASHBOARD_PAGE, null));
	}
}
