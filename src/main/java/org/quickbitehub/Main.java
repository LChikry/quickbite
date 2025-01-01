package org.quickbitehub;

import org.quickbitehub.utils.MessageHandler;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static org.quickbitehub.utils.MessageHandler;

public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			QuickBite quickBiteBot = new QuickBite();

			botsApplication.registerBot(quickBiteBot.getBotToken(), quickBiteBot);
			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> QuickBite.scheduler.shutdown()));
	}
}