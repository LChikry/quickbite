package org.quickbitehub.communicator;

import org.quickbitehub.authentication.Account;
import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.order.OrderStatus;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.util.Objects;

public class PageFactory {
	public static Integer viewAuthenticationPage(Long telegramId, Integer messageId) {
		String msg = Emoji.LOCK_WITH_KEY.getCode() + " *Authenticate*\n" +
				"\n" +
				"Welcome to QuickBite, where you can Skip the Line, Save the Time for What Matters Most\\.\n" +
				"Sign in or Sing up, so you can benefit from our services that will streamline your food ordering process for greater life quality " + Emoji.SMILING_FACE.getCode();

		if (null == messageId) {
			return (Objects.requireNonNull(
						MessageHandler.sendInlineKeyboard(
							telegramId,
							msg,
							KeyboardFactory.getAuthenticationKeyboard(),
							TimeConstants.NO_TIME.time()
						)
					)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, msg, KeyboardFactory.getAuthenticationKeyboard());
		return null;
	}
	public static Integer viewSignInPage(Long telegramId, Integer messageId, String email, String hiddenPassword) {
		assert (!Authentication.isSessionAuthenticated(telegramId));
		String message = Emoji.GOLDEN_KEY.getCode() + " *Sign In*\n" +
				"\n" +
				"Click to provide your credentials\\.";
		if (null == messageId) {
			return (Objects.requireNonNull(
					MessageHandler.sendInlineKeyboard(
							telegramId,
							message,
							KeyboardFactory.getSignInKeyboard(email, hiddenPassword),
							TimeConstants.NO_TIME.time()
					)
			)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, message, KeyboardFactory.getSignInKeyboard(email, hiddenPassword));
		return null;
	}
	public static void updateSignInPage(Long telegramId, Integer messageId, String email, String hiddenPassword) {
		MessageHandler.editInlineKeyboardOnly(telegramId, messageId, KeyboardFactory.getSignInKeyboard(email, hiddenPassword));
	}
	public static Integer viewSignUpPage(Long telegramId, Integer messageId, String email, String hiddenPassword, String firstName, String lastName, String middleNames) {
		assert (!Authentication.isSessionAuthenticated(telegramId));
		String message = Emoji.REGISTRATION_PAPER.getCode() + " *Sign Up*\n" +
				"\n" +
				"Click to provide your credentials\\.\n";
		if (null == messageId) {
			return (Objects.requireNonNull(
					MessageHandler.sendInlineKeyboard(
							telegramId,
							message,
							KeyboardFactory.getSignUpKeyboard(email, hiddenPassword, firstName, lastName, middleNames),
							TimeConstants.NO_TIME.time()
					)
			)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, message, KeyboardFactory.getSignUpKeyboard(email, hiddenPassword, firstName, lastName, middleNames));
		return null;
	}
	public static void updateSignUpPage(Long telegramId, Integer messageId, String email, String hiddenPassword, String firstName, String lastName, String middleNames) {
		MessageHandler.editInlineKeyboardOnly(telegramId, messageId, KeyboardFactory.getSignUpKeyboard(email, hiddenPassword, firstName, lastName, middleNames));
	}
	public static Integer viewDashboardPage(Long telegramId, Integer messageId) {
		assert (Authentication.isSessionAuthenticated(telegramId));
		// task add money and order values
		String message = "*Welcome to Dashboard* " + Emoji.YELLOW_STARS.getCode() +
				"\n" +
				"\n*" + Authentication.getSessionAccount(telegramId).getUser().getUserFullName() + "*\\, from here you can take control of everything\\!" +
				"\n" +
				"\nMoney spent so far\\: *" + "*" +
				"\n" +
				"\n" + OrderStatus.PENDING.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.IN_PREPARATION.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.READY.getStatus() + " orders\\: *" + "*" +
				"\n" + OrderStatus.CANCELED.getStatus() + " orders\\: *" + "*";

		if (null == messageId) {
			return (Objects.requireNonNull(
						MessageHandler.sendInlineKeyboard(
							telegramId,
							message,
							KeyboardFactory.getDashboardPageKeyboard(),
							TimeConstants.NO_TIME.time()
						)
				)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, message, KeyboardFactory.getDashboardPageKeyboard());
		return null;
	}
	public static void viewFavoriteRestaurants(Long telegramId) {
		Account account = Authentication.userSession.get(telegramId);
		assert (account != null);

		String message;
		if (!account.getFavoriteRestaurants().isEmpty()) {
			message = "Which restaurant you want to order from\\?";
		} else {
			message = "We didn\\'t find any favorite restaurants\\, click on search to search for restaurants and add some favorite ones";
		}
		MessageHandler.sendReplyKeyboard(telegramId, message,
				KeyboardFactory.getRestaurantChoicesKeyboard(account.getFavoriteRestaurants()),
				TimeConstants.NO_TIME.time());
	}
	public static Integer viewSettingsPage(Long telegramId, Integer messageId) {
		assert (Authentication.isSessionAuthenticated(telegramId));
		Account userAccount = Authentication.getSessionAccount(telegramId);
		LocalDate signUpDate = userAccount.getAccountSignUpDate();
		String message = "*Settings*" +
				"\n" +
				"\nClick to change your information\\." +
				"\n" +
				"\nYou have been with us since " + signUpDate.getDayOfMonth() + " " + signUpDate.getMonth() + "\\, " + signUpDate.getYear() + " " + Emoji.HEART_FIGURE.getCode();
		if (null == messageId) {
			return (Objects.requireNonNull(
						MessageHandler.sendInlineKeyboard(
							telegramId,
							message,
							KeyboardFactory.getCustomerSettingsKeyboard(userAccount),
							TimeConstants.NO_TIME.time()
						)
					)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, message, KeyboardFactory.getCustomerSettingsKeyboard(userAccount));
		return null;
	}
	public static Integer viewHelpPage(Long telegramId, Integer messageId) {
		// task: correct urls as soon as you create the documentation
		String msg = "_*Support Page*_" +
				"\n" +
				"\n    *How do I use QuickBite\\?*" +
				"\nYou can watch tutorials on YouTube or you can read the documentation [for users](www.google.com) or [for restaurant owners](www.google.com)\\." +
				"\n" +
				"\n    *What are the transaction and operation fees\\?*" +
				"\nQuickBite charges a 00\\.00% fees in all kind of operations and transactions\\. QuickBite gets its funding solely from donations\\. However\\, services that may be used during order payment may charge fees\\, which is out of our control\\." +
				"\n" +
				"\n    *Why order can\\'t be canceled\\?*" +
				"\nWhen you issue an order\\, and this order has been accepted by the restaurant\\, you cannot cancel the order anymore since this behavior will damage restaurants\\. However\\, you can cancel pending orders that have been not answered yet by the restaurant\\." +
				"\n" +
				"\n" +
				"\n_If you still have questions\\, or you encountered a problem\\, please do not hesitate to look at the documentation or contact us at *support@quickbitehub\\.org*_";

		if (null == messageId) {
			return (Objects.requireNonNull(
						MessageHandler.sendInlineKeyboard(
								telegramId,
								msg,
								KeyboardFactory.getHelpPageKeyboard(),
								TimeConstants.NO_TIME.time()
						)
					)
			).getMessageId();
		}
		MessageHandler.editInlineKeyboardAndMessage(telegramId, messageId, msg, KeyboardFactory.getHelpPageKeyboard());
		return null;
	}
}