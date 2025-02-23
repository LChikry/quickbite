package org.quickbitehub.authentication.signin;

import org.apache.commons.lang3.tuple.Pair;
import org.quickbitehub.account.Account;
import org.quickbitehub.authentication.AuthMessages;
import org.quickbitehub.authentication.AuthenticationService;
import org.quickbitehub.state.State;
import org.quickbitehub.state.UserState;
import org.quickbitehub.communicator.Emoji;
import org.quickbitehub.communicator.MessageHandler;
import org.quickbitehub.communicator.PageFactory;
import org.quickbitehub.communicator.TimeConstants;

enum SignInService {
	INSTANCE;
	private final AuthenticationService authService = AuthenticationService.getInstance();

	void viewSignInPage(Long chatId, Integer messageId) {
		authService.clearChatAuthState(chatId);
		var kbId = PageFactory.viewSignInPage(chatId, messageId, null, null);
		if (kbId == null) kbId = messageId;
		authService.addAuthStateWithValue(chatId, UserState.SIGNIN_PAGE, String.valueOf(kbId.intValue()));
		if (!kbId.equals(messageId)) State.updateKeyboardState(chatId, kbId, UserState.SIGNIN_PAGE);
	}
	void collectSignInCredentials(Long chatId, UserState authState, Integer answerMessageId, Integer promptMessageId, String credential) {
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

		String rawEmail = authService.getAuthStateValue(chatId, UserState.__GET_SIGNIN_EMAIL);
		String rawPassword = null;
		if (authService.getAuthStateValue(chatId, UserState.__GET_SIGNIN_PASSWORD) != null) {
			rawPassword = Emoji.PASSWORD_DOT.getCode().repeat(10);
		}
		Integer signInPageId = Integer.valueOf(authService.getAuthStateValue(chatId, UserState.SIGNIN_PAGE));
		PageFactory.updateSignInPage(chatId, signInPageId, rawEmail, rawPassword);
	}

	void confirmSignIn(Long chatId) {
		String pureEmail = authService.getAuthStateValue(chatId, UserState.__GET_SIGNIN_EMAIL);
		pureEmail = Account.formatEmail(pureEmail);
		String rawPassword = authService.getAuthStateValue(chatId, UserState.__GET_SIGNIN_PASSWORD);
		Account userAccount = Account.authenticate(chatId, pureEmail, rawPassword);
		if (userAccount == null) {
			MessageHandler.sendShortNotice(chatId, AuthMessages.FAILED_SIGNIN.getPrompt());
			State.applyImmediateState(chatId, Pair.of(UserState.__CANCEL_CURRENT_OPERATION_WITHOUT_NOTICE, null));
			return;
		}
		authService.removeChatAuthState(chatId);
		authService.addChatAccount(chatId, userAccount);

		MessageHandler.sendShortNotice(chatId, AuthMessages.SUCCESSFUL_SIGNIN.getPrompt());
		State.popAuthRelatedState(chatId);
		State.applyImmediateState(chatId, Pair.of(UserState.DASHBOARD_PAGE, null));
	}
}
