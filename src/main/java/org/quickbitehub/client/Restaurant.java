package org.quickbitehub.client;

import org.quickbitehub.authentication.DBCredentials;

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

	public static HashMap<String, Restaurant> allRestaurants = getAllRestaurantsFromDB();

	public Restaurant(String restaurantId, String restaurantName) {
		this.RESTAURANT_ID = restaurantId;
		this.restaurantName = restaurantName;
	}

	public static void insertRestaurant(Restaurant restaurant) {
		allRestaurants.put(restaurant.getRestaurantId(), restaurant);
		//task: insert into db
	}

	public static void viewRestaurants(Long telegramId, String query) {

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
				String name = resultSet.getString("restaurant_name");

				restaurants.put(restaurantId, new Restaurant(restaurantId, name));
			}

			query = "SELECT * FROM RestaurantAvailability";
			statement = connection.prepareStatement(query);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String restaurantId = resultSet.getString("restaurant_id");
				Restaurant rest = restaurants.get(restaurantId);
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
