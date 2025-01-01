package org.quickbitehub;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

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
	}
}