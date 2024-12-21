package org.quickbitehub;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;

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

		int customerId = 1; // Replace with a valid customer_id
		double res = getCustomerBalance(customerId);
		if (res != -1) {
			System.out.println("Customer balance: " + res);
		}
		else {
			System.out.println("Customer not found.");
		}
	}




		/*String url="jdbc:postgresql://localhost:5432/[database name]";
		String username="postgres";
		String password="";

		try {
			Connection con = DriverManager.getConnection(url, username, password);
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT customer_balance FROM Customer");
			rs.next();
			String name = rs.getString(1);
			System.out.println(name);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}*/

			// Function to get the balance of a customer by ID

	public static double getCustomerBalance (int customerId) {
				// Database connection details
				String url = "jdbc:postgresql://localhost:5432/your_databasename"; // Update with your database name
				String user = "your_username"; // Update with your username
				String password = "your_password"; // Update with your password

				// SQL query
				String query = "SELECT customer_balance FROM Customer WHERE customer_id = ?";
				double balance = -1.00; // Default value if customer is not found

				try (
						Connection connection = DriverManager.getConnection(url, user, password);
						PreparedStatement statement = connection.prepareStatement(query)
				) {
					// Set the customer_id parameter in the query
					statement.setInt(1, customerId);

					// Execute the query
					try (ResultSet rs = statement.executeQuery()) {

						if (rs.next()) {
							// Retrieve the balance from the result set
							balance = rs.getDouble("customer_balance");
						}

					}
				} catch (SQLException e) {
					// Handle SQL exception
					System.err.println("Database error: " + e.getMessage());
				}

				return balance;
			}



}