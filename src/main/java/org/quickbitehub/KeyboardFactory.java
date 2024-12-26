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
		keyboards.put(KeyboardType.SIGN_IN_UP, getSignInUpKeyboard());
		keyboards.put(KeyboardType.FORCE_REPLY, getForceReplyKeyboard());
		return keyboards;
	}

	static private InlineKeyboardMarkup getSignInUpKeyboard() {
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

	static private ForceReplyKeyboard getForceReplyKeyboard() {
		return ForceReplyKeyboard.builder()
				.forceReply(true)
				.build();
	}
}
