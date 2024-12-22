package org.quickbitehub;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.HashMap;

public class KeyboardFactory {
	static public HashMap<KeyboardType, InlineKeyboardMarkup> getKeyboardList() {
		HashMap<KeyboardType, InlineKeyboardMarkup> keyboards = new HashMap<>();
		keyboards.put(KeyboardType.LOGIN, getLogInKeyboard());


		return keyboards;
	}

	static private InlineKeyboardMarkup getLogInKeyboard() {
		var emailButton = InlineKeyboardButton
				.builder()
				.text("Email")
				.switchInlineQueryCurrentChat("Enter email: ")
				.build();

		var passwordButtom = InlineKeyboardButton
				.builder()
				.text("Password")
				.switchInlineQueryCurrentChat("Enter password: ")
				.build();

		var signUpButton = InlineKeyboardButton
				.builder()
				.text("Sign Up")
				.callbackData("-----signup")
				.build();

		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(emailButton))
				.keyboardRow(new InlineKeyboardRow(passwordButtom))
				.keyboardRow(new InlineKeyboardRow(signUpButton))
				.build();

		return keyboard;
	}
}
