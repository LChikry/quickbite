package org.quickbitehub.provider;

import org.quickbitehub.QuickBite;
import org.quickbitehub.authentication.Account;
import org.quickbitehub.authentication.Authentication;
import org.quickbitehub.authentication.DBCredentials;
import org.quickbitehub.utils.KeyboardFactory;
import org.quickbitehub.utils.MessageHandler;
import org.quickbitehub.utils.Emoji;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashMap;

public class Restaurant {
	private final String RESTAURANT_ID;
	private String restaurantName;
	private HashMap<DaysOfWeek, OffsetTime> restaurantOpeningTime = new HashMap<>();
	private HashMap<DaysOfWeek, OffsetTime> restaurantClosingTime = new HashMap<>();

	public static HashMap<String, Restaurant> allRestaurants = getAllRestaurantsFromDB(); // RestaurantName -> Restaurant

	public Restaurant(String restaurantId, String restaurantName) {
		this.RESTAURANT_ID = restaurantId;
		this.restaurantName = restaurantName;
	}

	public static void insertRestaurant(Restaurant restaurant) {
		allRestaurants.put(restaurant.getRestaurantName(), restaurant);
		//task: insert into db
	}

	public static boolean viewRestaurants(Long telegramId, String query) {
		if (allRestaurants.isEmpty()) {
			String msg = Emoji.ORANGE_CIRCLE.getCode() + " Sorry, there is no restaurant currently operating in our bot\\.";
			MessageHandler.sendText(telegramId, msg, QuickBite.SHORT_DELAY_TIME_SEC);
			return false;
		}

		if (query == null) {
			Account account = Authentication.userSessions.get(telegramId);
			assert (account != null);
			if (account.getRecentUsedRestaurants().isEmpty()) {
				for (String restaurantName : Restaurant.allRestaurants.keySet()) {
					account.addRecentUsedRestaurant(restaurantName);
					if (account.getRecentUsedRestaurants().size() >= Account.MAX_RECENT_USED_RESTAURANT_LENGTH) break;
				}
			}
			String message = "Which restaurant you want to order from\\?";
			MessageHandler.sendReplyKeyboard(telegramId, message,
					KeyboardFactory.getRestaurantChoicesKeyboard(account.getRecentUsedRestaurants()),
					QuickBite.LONG_DELAY_TIME_SEC);
			return true;
		}
		// task: view the inline query results
		return true;
	}

	public static void viewRestaurantProducts(Long telegramId, String query) {

	}

	public boolean isOpen() {
		if (restaurantOpeningTime == null || restaurantClosingTime == null ||
			restaurantOpeningTime.isEmpty() || restaurantClosingTime.isEmpty()) {
			return false;
		}

		OffsetDateTime now = OffsetDateTime.now(restaurantOpeningTime.values().iterator().next().getOffset());
		DayOfWeek currentDay = now.getDayOfWeek();
		if (restaurantOpeningTime.get(currentDay.name()) == null &&
			restaurantClosingTime.get(currentDay.name()) == null) {
			return false;
		}

		OffsetTime currentTime = now.toOffsetTime();
		return (!currentTime.isBefore(restaurantOpeningTime.get(currentDay.name())) &&
				!currentTime.isAfter(restaurantClosingTime.get(currentDay.name())));
	}

	public String getRestaurantId() {
		return RESTAURANT_ID;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		// task: change it in the db
		this.restaurantName = restaurantName;
	}

	public static void fetchAllRestaurants() {
		Restaurant.allRestaurants = getAllRestaurantsFromDB();
	}

	private static HashMap<String, Restaurant> getAllRestaurantsFromDB() {
		String query = "SELECT * FROM Restaurant";
		HashMap<String, Restaurant> restaurants = new HashMap<>();
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String restaurantId = resultSet.getString("restaurant_id");
				String restaurantName = resultSet.getString("restaurant_name");

				restaurants.put(restaurantName, new Restaurant(restaurantId, restaurantName));
			}

			query = "SELECT * FROM RestaurantAvailability";
			statement = connection.prepareStatement(query);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String restaurantName = resultSet.getString("restaurant_name");
				Restaurant rest = restaurants.get(restaurantName);
				DaysOfWeek day = DaysOfWeek.valueOf(resultSet.getString("day_of_week"));
				OffsetTime open = resultSet.getObject("restaurant_opening_time", OffsetTime.class);
				OffsetTime close = resultSet.getObject("restaurant_closing_time", OffsetTime.class);
				rest.restaurantOpeningTime.put(day, open);
				rest.restaurantClosingTime.put(day, close);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return restaurants;
	}
}
