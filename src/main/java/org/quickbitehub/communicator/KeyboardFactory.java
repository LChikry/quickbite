package org.quickbitehub.communicator;

import org.quickbitehub.account.Account;
import org.quickbitehub.utils.LanguageType;
import org.quickbitehub.app.UserState;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {
	private static final InlineKeyboardButton backButton = InlineKeyboardButton.builder().text(Emoji.LEFT_TRANSPARENT_ARROW.getCode()+" Back").callbackData(UserState.__PREVIOUS_KEYBOARD.getState()).build();

	public static InlineKeyboardMarkup getAuthenticationKeyboard() {
		var signInButton = InlineKeyboardButton.builder().text("Sign In").callbackData(UserState.SIGNIN_PAGE.getState()).build();
		var signUpButton = InlineKeyboardButton.builder().text("Sign Up").callbackData(UserState.SIGNUP_PAGE.getState()).build();
		var helpButton = InlineKeyboardButton.builder().text("Help").callbackData(UserState.HELP_PAGE.getState()).build();
		var languageButton = InlineKeyboardButton.builder().text(Emoji.SPEECH_BALLOON.getCode() + " Language").callbackData(UserState.CHANGE_LANGUAGE.getState()).build();

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(signInButton))
				.keyboardRow(new InlineKeyboardRow(signUpButton))
				.keyboardRow(new InlineKeyboardRow(helpButton, languageButton))
				.build();
	}
	public static InlineKeyboardMarkup getSignInKeyboard(String email, String hiddenPassword) {
		if (email == null) email = "Email";
		if (hiddenPassword == null) hiddenPassword = "Password";
		var emailButton = InlineKeyboardButton.builder().text(email).callbackData(UserState.__SET_SIGNIN_EMAIL.getState()).build();
		var passwordButton = InlineKeyboardButton.builder().text(hiddenPassword).callbackData(UserState.__SET_SIGNIN_PASSWORD.getState()).build();
		var confirmButton = InlineKeyboardButton.builder().text("Confirm").callbackData(UserState.__CONFIRM_SIGNIN.getState()).build();
		var signUpButton = InlineKeyboardButton.builder().text("Sign Up").callbackData(UserState.SIGNUP_PAGE.getState()).build();

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(emailButton))
				.keyboardRow(new InlineKeyboardRow(passwordButton))
				.keyboardRow(new InlineKeyboardRow(confirmButton))
				.keyboardRow(new InlineKeyboardRow(backButton, signUpButton))
				.build();
	}
	public static InlineKeyboardMarkup getSignUpKeyboard(String email, String hiddenPassword, String firstName, String lastName, String middleNames) {
		if (email == null) email = "Email";
		if (hiddenPassword == null) hiddenPassword = "Password";
		if (firstName == null) firstName = "First Name";
		else firstName = "First Name: " + firstName;
		if (lastName == null) lastName = "Last Name";
		else lastName = "Last Name: " + lastName;
		if (middleNames == null) middleNames = "Middle Name(s) (optional)";
		else middleNames = "Middle Names(s): " + middleNames;
		var emailButton = InlineKeyboardButton.builder().text(email).callbackData(UserState.__SET_SIGNUP_EMAIL.getState()).build();
		var passwordButton = InlineKeyboardButton.builder().text(hiddenPassword).callbackData(UserState.__SET_SIGNUP_PASSWORD.getState()).build();
		var firstNameButton = InlineKeyboardButton.builder().text(firstName).callbackData(UserState.__SET_SIGNUP_FIRST_NAME.getState()).build();
		var lastNameButton = InlineKeyboardButton.builder().text(lastName).callbackData(UserState.__SET_SIGNUP_LAST_NAME.getState()).build();
		var middleNamesButton = InlineKeyboardButton.builder().text(middleNames).callbackData(UserState.__SET_SIGNUP_MIDDLE_NAMES.getState()).build();
		var confirmButton = InlineKeyboardButton.builder().text("Confirm").callbackData(UserState.__CONFIRM_SIGNUP.getState()).build();
		var signInButton = InlineKeyboardButton.builder().text("Sign In").callbackData(UserState.SIGNIN_PAGE.getState()).build();

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(emailButton))
				.keyboardRow(new InlineKeyboardRow(passwordButton))
				.keyboardRow(new InlineKeyboardRow(firstNameButton))
				.keyboardRow(new InlineKeyboardRow(lastNameButton))
				.keyboardRow(new InlineKeyboardRow(middleNamesButton))
				.keyboardRow(new InlineKeyboardRow(confirmButton))
				.keyboardRow(new InlineKeyboardRow(backButton, signInButton))
				.build();
	}
	public static InlineKeyboardMarkup getHelpPageKeyboard() {
		var usersDoc = InlineKeyboardButton
				.builder()
				.text("Docs for Users")
				.url("www.google.com")
				.build();
		var ownersDoc = InlineKeyboardButton
				.builder()
				.text("Docs for Restaurant Owners")
				.url("www.google.com")
				.build();
		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(usersDoc, ownersDoc))
				.keyboardRow(new InlineKeyboardRow(backButton))
				.build();
	}
	public static InlineKeyboardMarkup getDashboardPageKeyboard() {
		var orderButton = InlineKeyboardButton.builder().text("Issue an Order").callbackData(UserState.SELECT_FAVORITE_RESTAURANT.getState()).build();

		var cancelOrderButton = InlineKeyboardButton.builder().text("Cancel Pending Order").callbackData(UserState.CANCEL_PENDING_ORDER.getState()).build();
		var manageOrdersButton = InlineKeyboardButton.builder().text("Manage Orders").callbackData(UserState.MANAGE_ORDERS_PAGE.getState()).build();

		var settingsButton = InlineKeyboardButton.builder().text("Settings").callbackData(UserState.SETTINGS_PAGE.getState()).build();
		var helpButton = InlineKeyboardButton.builder().text("Help").callbackData(UserState.HELP_PAGE.getState()).build();
		var signOutButton = InlineKeyboardButton.builder().text("Sign Out").callbackData(UserState.AUTHENTICATION_SIGNOUT.getState()).build();

		var cancelButton = InlineKeyboardButton.builder().text("Cancel Current Operation").callbackData(UserState.CANCEL_CURRENT_OPERATION_WITH_NOTICE.getState()).build();
		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(orderButton))
				.keyboardRow(new InlineKeyboardRow(cancelOrderButton, manageOrdersButton))
				.keyboardRow(new InlineKeyboardRow(settingsButton, helpButton, signOutButton))
				.keyboardRow(new InlineKeyboardRow(cancelButton))
				.build();
	}
	public static ForceReplyKeyboard getForceReplyKeyboard() {
		return ForceReplyKeyboard.builder()
				.forceReply(true)
				.build();
	}
	public static ReplyKeyboardMarkup getRestaurantChoicesKeyboard(ArrayList<String> recentUsedRestaurantNames) {
		List<KeyboardRow> kbRow = new ArrayList<>();
		ReplyKeyboardMarkup replyKb = new ReplyKeyboardMarkup(kbRow);
		for (String restaurantName : recentUsedRestaurantNames) {
			var restButton = KeyboardButton.builder()
					.text(restaurantName)
					.build();
			kbRow.add(new KeyboardRow(restButton));
		}
		String text = Emoji.LEFT_MAGNIFIER.getCode().repeat(2) + " No favorite Rest. Search... " + Emoji.RIGHT_MAGNIFIER.getCode().repeat(2);
		var restButton = KeyboardButton.builder()
				.text(text)
				.build();
		kbRow.add(new KeyboardRow(restButton));

		replyKb.setOneTimeKeyboard(true);
		replyKb.setKeyboard(kbRow);
		return replyKb;
	}
	public static InlineKeyboardMarkup getCustomerSettingsKeyboard(Account customerAccount) {
		String email = customerAccount.getUnformattedEmail();
		var emailButton = InlineKeyboardButton.builder().text("Email: " + email).callbackData(UserState.CHANGE_EMAIL.getState()).build();
		var passwordButton = InlineKeyboardButton.builder().text("Change Password").callbackData(UserState.CHANGE_PASSWORD.getState()).build();

		String firstName = customerAccount.getUser().getFirstName();
		String lastName = customerAccount.getUser().getLastName();
		String middleNames = customerAccount.getUser().getMiddleNames();
		if (middleNames.isBlank()) middleNames = "-";
		var firstNameButton = InlineKeyboardButton.builder().text("First Name: " + firstName).callbackData(UserState.CHANGE_FIRST_NAME.getState()).build();
		var lastNameButton = InlineKeyboardButton.builder().text("Last Name: " + lastName).callbackData(UserState.CHANGE_LAST_NAME.getState()).build();
		var middleNamesButton = InlineKeyboardButton.builder().text("Middle Names: " + middleNames).callbackData(UserState.CHANGE_MIDDLE_NAMES.getState()).build();

		LanguageType interfaceLanguage = customerAccount.getInterfaceLanguage();
		var languageButton = InlineKeyboardButton.builder().text(Emoji.SPEECH_BALLOON.getCode() + " " +interfaceLanguage.getName()).callbackData(UserState.CHANGE_LANGUAGE.getState()).build();
		var favoriteRestaurantsButton = InlineKeyboardButton.builder().text("Favorites").callbackData(UserState.CHANGE_FAVORITE_RESTAURANTS.getState()).build();
		List<InlineKeyboardButton> restAndLanguageButtons = new ArrayList<>(2);
		restAndLanguageButtons.add(backButton);
		restAndLanguageButtons.add(favoriteRestaurantsButton);
		restAndLanguageButtons.add(languageButton);

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(emailButton))
				.keyboardRow(new InlineKeyboardRow(firstNameButton))
				.keyboardRow(new InlineKeyboardRow(lastNameButton))
				.keyboardRow(new InlineKeyboardRow(middleNamesButton))
				.keyboardRow(new InlineKeyboardRow(passwordButton))
				.keyboardRow(new InlineKeyboardRow(restAndLanguageButtons))
				.build();
	}
}