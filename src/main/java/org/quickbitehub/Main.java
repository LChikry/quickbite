package org.quickbitehub;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;
import java.util.HashMap;


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

		int customerId = 4; // Replace with a valid customer_id
		double res = getCustomerBalance(customerId);
		if (res != -1) {
			System.out.println("Customer balance: " + res);
		}
		else {
			System.out.println("Customer not found.");
		}


        HashMap<Integer, HashMap<String, Object>> customers = getAllCustomers();

        if (!customers.isEmpty()) {
            for (HashMap.Entry<Integer, HashMap<String, Object>> entry : customers.entrySet()) {
                customerId = entry.getKey();
                HashMap<String, Object> customerData = entry.getValue();
                System.out.println("Customer ID: " + customerId);
                System.out.println("First Name: " + customerData.get("cus_fname"));
                System.out.println("Balance: " + customerData.get("customer_balance"));
                System.out.println("-----------------------------");
            }
        } else {
            System.out.println("No customers found.");
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
				String url = "jdbc:postgresql://localhost:5432/test2"; // Update with your database name
				String user = "postgres"; // Update with your username
				String password = ""; // Update with your password

				// SQL query
				String query = "SELECT customer_balance FROM Customer WHERE user_id = ?";
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


        // Method to get all customers and store them in a HashMap
        public static HashMap<Integer, HashMap<String, Object>> getAllCustomers() {
            String url = "jdbc:postgresql://localhost:5432/test2"; // Your DB details
            String user = "postgres"; // Your DB username
            String password = ""; // Your DB password

            String query = "SELECT customer_id, cus_fname, customer_balance FROM Customer";
            HashMap<Integer, HashMap<String, Object>> customers = new HashMap<>();  // HashMap to store customer data by customer_id

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                // Loop through the result set and add rows to the HashMap
                while (resultSet.next()) {
                    int customerId = resultSet.getInt("customer_id");

                    // Create a HashMap for each row
                    HashMap<String, Object> customerData = new HashMap<>();
	                customerData.put("cus_fname", resultSet.getString("cus_fname"));
	                customerData.put("customer_balance", resultSet.getDouble("customer_balance"));

	                // Add this HashMap to the customers map using customer_id as the key
                    customers.put(customerId, customerData);

                }

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }

            return customers;
        }
}