package org.quickbitehub;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.app.QuickBite;
import org.quickbitehub.communicator.MessageHandler;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			QuickBite quickBiteBot = new QuickBite();

			botsApplication.registerBot(Dotenv.load().get("BOT_TOKEN"), quickBiteBot);
			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MessageHandler.shutdownScheduler();
	}
}