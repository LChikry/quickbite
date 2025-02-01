package org.quickbitehub.communicator;

public class MessageFactory {

	public static void sendIncorrectOperationNotice(Long telegramId) {
		String message = Emoji.ORANGE_CIRCLE.getCode() + " You cannot perform this operation, please follow the standard procedures and operations.";
		MessageHandler.sendShortNotice(telegramId, message);
	}
	public static void sendIncorrectInputNotice(Long telegramId) {
		String message = Emoji.RED_CIRCLE.getCode() + " Invalid input! Please try again.";
		MessageHandler.sendShortNotice(telegramId, message);
	}
}
