package org.quickbitehub.communicator;

public class MessageFactory {

	public static void sendIncorrectOperationNotice(Long telegramId) {
		String message = Emoji.ORANGE_CIRCLE.getCode() + " You cannot perform this operation\\, please follow the standard procedures and operations\\.";
		MessageHandler.sendText(telegramId, message, SHORT_DELAY_TIME_SEC);
	}
}
