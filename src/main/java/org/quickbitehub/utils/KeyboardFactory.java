package org.quickbitehub.utils;

import org.quickbitehub.consumer.UserState;
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
	public static InlineKeyboardMarkup getSignInUpKeyboard() {
		var logInButton = InlineKeyboardButton
				.builder()
				.text("Sign In")
				.callbackData(UserState.AUTHENTICATION_SIGNIN.getState())
				.build();

		var signUpButton = InlineKeyboardButton
				.builder()
				.text("Sign Up")
				.callbackData(UserState.AUTHENTICATION_SIGNUP.getState())
				.build();

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(logInButton))
				.keyboardRow(new InlineKeyboardRow(signUpButton))
				.build();
	}


	public static InlineKeyboardMarkup getHelpPageKeyboard() {
		var logInButton = InlineKeyboardButton
				.builder()
				.text("Docs for Users")
				.url("www.google.com")
				.build();

		var signUpButton = InlineKeyboardButton
				.builder()
				.text("Docs for Restaurant Owners")
				.url("www.google.com")
				.build();

		List<InlineKeyboardButton> buttons = new ArrayList<>(2);
		buttons.add(logInButton);
		buttons.add(signUpButton);

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(buttons))
				.build();
	}

	public static InlineKeyboardMarkup getDashboardPageKeyboard() {
		var orderButton = InlineKeyboardButton.builder().text("Issue an Order").callbackData(UserState.ISSUE_ORDER.getState()).build();

		var cancelOrderButton = InlineKeyboardButton.builder().text("Cancel Pending Order").callbackData(UserState.CANCEL_PENDING_ORDER.getState()).build();
		var manageOrdersButton = InlineKeyboardButton.builder().text("Manage Orders").callbackData(UserState.MANAGE_ORDERS_PAGE.getState()).build();
		List<InlineKeyboardButton> orderActionButtoms = new ArrayList<>(2);
		orderActionButtoms.add(cancelOrderButton);
		orderActionButtoms.add(manageOrdersButton);

		var settingsButton = InlineKeyboardButton.builder().text("Settings").callbackData(UserState.SETTINGS_PAGE.getState()).build();
		var helpButton = InlineKeyboardButton.builder().text("Help").callbackData(UserState.HELP_PAGE.getState()).build();
		var signOutButton = InlineKeyboardButton.builder().text("Sign Out").callbackData(UserState.AUTHENTICATION_SIGNOUT.getState()).build();
		List<InlineKeyboardButton> immediateButtons = new ArrayList<>(2);
		immediateButtons.add(settingsButton);
		immediateButtons.add(helpButton);
		immediateButtons.add(signOutButton);

		var cancelButton = InlineKeyboardButton.builder().text("Cancel Current Operation").callbackData(UserState.CANCEL_CURRENT_OPERATION.getState()).build();
		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(orderButton))
				.keyboardRow(new InlineKeyboardRow(orderActionButtoms))
				.keyboardRow(new InlineKeyboardRow(immediateButtons))
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
		String text = Emoji.LEFT_MAGNIFIER.getCode().repeat(2) + " Click to Search For Others... " + Emoji.RIGHT_MAGNIFIER.getCode().repeat(2);
		var restButton = KeyboardButton.builder()
				.text(text)
				.build();
		kbRow.add(new KeyboardRow(restButton));

		replyKb.setOneTimeKeyboard(true);
		replyKb.setKeyboard(kbRow);
		return replyKb;
	}
}
