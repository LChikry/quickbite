package org.quickbitehub;

import org.quickbitehub.authentication.Account;
import org.quickbitehub.client.Customer;
import org.quickbitehub.client.Employee;
import org.quickbitehub.order.Order;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;
import java.util.HashMap;


public class Main {
	public static void main(String[] args) {
		// Using try-with-resources to allow autoclose to run upon finishing
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			QuickBite quickBiteBot = new QuickBite();
			Account.fetchAllAccounts();
			Customer.fetchAllCustomers();
			Employee.fetchAllEmployees();
			Order.fetchAllCurrentOrders();
			Order.fetchAllPast30DayOrders();
			botsApplication.registerBot(quickBiteBot.getBotToken(), quickBiteBot);

			System.out.println("The Bot is successfully started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}