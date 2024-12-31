package org.quickbitehub.utils;

import org.quickbitehub.CBQData;
import org.quickbitehub.client.Restaurant;
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
				.callbackData(CBQData.SIGNING_PROCESS.getData())
				.build();

		var signUpButton = InlineKeyboardButton
				.builder()
				.text("Sign Up")
				.callbackData(CBQData.SIGNUP_PROCESS.getData())
				.build();

		return InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(logInButton))
				.keyboardRow(new InlineKeyboardRow(signUpButton))
				.build();
	}

	public static ForceReplyKeyboard getForceReplyKeyboard() {
		return ForceReplyKeyboard.builder()
				.forceReply(true)
				.build();
	}

	public static ReplyKeyboardMarkup getRestaurantChoicesKeyboard(ArrayList<String> recentUsedRestaurantsId) {
		List<KeyboardRow> kbRow = new ArrayList<>();
		ReplyKeyboardMarkup replyKb = new ReplyKeyboardMarkup(kbRow);
		for (String id : recentUsedRestaurantsId) {
			var restButton = KeyboardButton.builder()
					.text(Restaurant.allRestaurants.get(id).getRestaurantName())
					.build();

			kbRow.add(new KeyboardRow(restButton));
		}

		replyKb.setOneTimeKeyboard(true);
		replyKb.setKeyboard(kbRow);
		return replyKb;
	}
}
