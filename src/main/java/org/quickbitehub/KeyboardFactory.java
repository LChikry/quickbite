package org.quickbitehub;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.HashMap;

public class KeyboardFactory {
	static public HashMap<KeyboardType, ReplyKeyboard> getKeyboardList() {
		HashMap<KeyboardType, ReplyKeyboard> keyboards = new HashMap<>();
		keyboards.put(KeyboardType.LOGIN, getLogInKeyboard());
		keyboards.put(KeyboardType.FORCE_REPLY, getForceReplyKeyboard());
		return keyboards;
	}

	static private InlineKeyboardMarkup getLogInKeyboard() {
		var logInButton = InlineKeyboardButton
				.builder()
				.text("Sign In")
				.callbackData(CBQData.SIGNING_MENU.getData())
				.build();

		var signUpButton = InlineKeyboardButton
				.builder()
				.text("Sign Up")
				.callbackData(CBQData.SIGN_UP_MENU.getData())
				.build();

		InlineKeyboardMarkup keyboard = InlineKeyboardMarkup
				.builder()
				.keyboardRow(new InlineKeyboardRow(logInButton))
				.keyboardRow(new InlineKeyboardRow(signUpButton))
				.build();

		return keyboard;
	}

	static private ForceReplyKeyboard getForceReplyKeyboard() {
		ForceReplyKeyboard kb = ForceReplyKeyboard.builder()
				.forceReply(true)
				.build();

		return kb;
	}
}
