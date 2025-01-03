package org.quickbitehub.communicator;

import org.quickbitehub.app.QuickBite;
import org.quickbitehub.authentication.Account;
import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.order.OrderStatus;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PageFactory {
	public static final long SHORT_DELAY_TIME_SEC = 8; // in seconds
	public static final long STANDARD_DELAY_TIME_SEC = 30; // in seconds
	public static final long LONG_DELAY_TIME_SEC = 90; // in seconds
	public static final long LARGE_DELAY_TIME_SEC = 180; // in seconds
	public static final long NO_DELAY_TIME= 0; // in seconds

	public static Message viewAuthenticationPage(Long telegramId) {
		String msg = """
				    *_Authenticate_*
				
				Welcome to QuickBite, where you can Skip the Line, Save the Time for What Matters Most\\.
				Sign in or Sing up, so you can benefit from our services that will streamline your food ordering process for greater life quality \ud83d\ude01""";

		return MessageHandler.sendInlineKeyboard(telegramId, msg, KeyboardFactory.getSignInUpKeyboard(),
				STANDARD_DELAY_TIME_SEC);
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
			return (Objects.requireNonNull(MessageHandler.sendInlineKeyboard(telegramId, message, KeyboardFactory.getDashboardPageKeyboard(), NO_DELAY_TIME))).getMessageId();
		}
		MessageHandler.editInlineKeyboard(telegramId, messageId, message, KeyboardFactory.getDashboardPageKeyboard());
		return null;
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
			return (Objects.requireNonNull(MessageHandler.sendInlineKeyboard(telegramId, message, KeyboardFactory.getCustomerSettingsKeyboard(userAccount), NO_DELAY_TIME))).getMessageId();
		}
		MessageHandler.editInlineKeyboard(telegramId, messageId, message, KeyboardFactory.getCustomerSettingsKeyboard(userAccount));
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
			return (Objects.requireNonNull(MessageHandler.sendInlineKeyboard(telegramId, msg, KeyboardFactory.getHelpPageKeyboard(), LARGE_DELAY_TIME_SEC))).getMessageId();
		}
		MessageHandler.editInlineKeyboard(telegramId, messageId, msg, KeyboardFactory.getHelpPageKeyboard());
		return null;
	}
}
