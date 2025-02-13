package org.quickbitehub;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.app.*;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.authentication.AuthenticationService;
import org.quickbitehub.communicator.MessageHandler;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import com.password4j.*;


public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			// Singleton Objects
			AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());

			QuickBite quickBiteBot = new QuickBite(authController);
			botsApplication.registerBot(Dotenv.load().get("BOT_TOKEN"), quickBiteBot);

			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
			MessageHandler.shutdownScheduler();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}