package org.quickbitehub.order;

import org.quickbitehub.authentication.DBCredentials;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.HashMap;

public class Order {
	private final String ORDER_ID;
	private final OffsetDateTime ORDER_TIMESTAMP;
	private final String ORDER_TYPE;
	private MonetaryAmount orderTotalValue;
	private String currency = "MAD";
	private String orderStatus;
	private final String RESTAURANT_ID;
	private String employeeId;
	private final String CUSTOMER_ID;

	public static HashMap<String, Order> currentOrders = getCurrentOrders(); // orderId -> Order
	public static HashMap<String, Order> past30DayOrders = getPast30DayOrders(); // orderId -> Order

	public Order(String orderId, OffsetDateTime orderTimestmap, String orderType, MonetaryAmount orderTotalValue, String currency, String orderStatus, String restaurantId, String employeeId, String customerId) {
		this.ORDER_ID = orderId;
		this.ORDER_TIMESTAMP = orderTimestmap;
		this.ORDER_TYPE = orderType;
		this.orderTotalValue = orderTotalValue;
		this.currency = currency;
		this.orderStatus = orderStatus;
		this.RESTAURANT_ID = restaurantId;
		this.employeeId = employeeId;
		this.CUSTOMER_ID = customerId;
	}
	public MonetaryAmount getOrderTotalValue() {
		return getOrderTotalValue(Integer.valueOf(this.ORDER_ID));
	}

	public static void fetchAllCurrentOrders() {
		currentOrders = getCurrentOrders();
	}

	public static void fetchAllPast30DayOrders() {
		past30DayOrders = getPast30DayOrders();
	}

	public static int insertOrder(String orderType, Integer restaurantId, Integer customerId) throws SQLException {
		String insertSQL = "INSERT INTO Orders (order_type, restaurant_id, customer_id) VALUES (?, ?, ?) RETURNING order_id";

		try (Connection con = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {

			// Now, insert into the Customer table using the user_id
			try (PreparedStatement insertStatement = con.prepareStatement(insertSQL)) {

				insertStatement.setString(1, orderType);
				insertStatement.setInt(2, restaurantId);
				insertStatement.setInt(3, customerId);

				// Execute the insert and get the user_id
				try (ResultSet rs = insertStatement.executeQuery()) {
					if (rs.next()) {
						return rs.getInt("order_id");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static void updateOrderStatus(Integer orderId, String newOrderStatus) {
		String updateSQL = "CALL update_order_status(?, ?)";

		try (Connection con = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
		     PreparedStatement updateStatement = con.prepareStatement(updateSQL)) {

			updateStatement.setInt(1, orderId);
			updateStatement.setString(2, newOrderStatus);

			// Execute the update
			int rowsUpdated = updateStatement.executeUpdate();
			System.out.println(rowsUpdated + " rows its status got updated");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updateOrderEmployeeId(Integer orderId, Integer employeeId) {
		String updateSQL = "UPDATE Orders SET employee_id = ? WHERE order_id = ?";

		try (Connection con = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
		     PreparedStatement updateStatement = con.prepareStatement(updateSQL)) {

			updateStatement.setInt(1, employeeId);
			updateStatement.setInt(2, orderId);

			// Execute the update
			int rowsUpdated = updateStatement.executeUpdate();
			System.out.println(rowsUpdated + " rows its employee_id got updated");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static MonetaryAmount getOrderTotalValue(Integer orderId) {
		MonetaryAmount monetaryAmount = null;
		try (Connection conn = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {
            // Prepare the callable statement for the function
            String sql = "{ ? = call get_order_total_value(?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                // Register the first parameter as an output parameter (NUMERIC)
                stmt.registerOutParameter(1, Types.NUMERIC);
                stmt.setInt(2, orderId);
                stmt.execute();
                BigDecimal totalValue = stmt.getBigDecimal(1);

	            monetaryAmount= Monetary.getDefaultAmountFactory()
                        .setCurrency(getOrderCurrency(orderId)) // Replace "USD" with your actual currency
                        .setNumber(totalValue)
                        .create();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

		return monetaryAmount;

	}

	public static String getOrderCurrency(Integer orderId) {
		String query = "SELECT currency FROM Orders WHERE order_id = ?";
		String orderCurrency = null;

		try (Connection conn = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(),
				DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo())) {
			// Prepare the statement
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				// Set the orderId parameter
				stmt.setInt(1, orderId);

				// Execute the query
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						// Retrieve the order_currency value
						orderCurrency = rs.getString("order_currency");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return orderCurrency;
	}

	private static HashMap<String, Order> getCurrentOrders() {
		String query = "SELECT * FROM Orders WHERE order_timestamp >= CURRENT_DATE AND order_timestamp < CURRENT_DATE + INTERVAL '1 day' OR order_status IN (?, ?)";
		HashMap<String, Order> orders = new HashMap<>();  // HashMap to store customer data by customer_id
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			assert DBCredentials.DB_URL.getDBInfo() != null;
			try (Connection connection = DriverManager.getConnection(DBCredentials.DB_URL.getDBInfo(), DBCredentials.DB_USER.getDBInfo(), DBCredentials.DB_PASSWORD.getDBInfo());
			     PreparedStatement updateStatement = connection.prepareStatement(query)) {

				updateStatement.setString(1, OrderStatus.PENDING.getStatus());
				updateStatement.setString(2, OrderStatus.IN_PREPARATION.getStatus());

				ResultSet resultSet = updateStatement.executeQuery();

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					String orderId = resultSet.getString("order_id");
					OffsetDateTime orderTimestamp = resultSet.getObject("order_timestamp", OffsetDateTime.class);
					String orderType = resultSet.getString("order_type");
					BigDecimal orderTotalValueDecimal = resultSet.getBigDecimal("order_total_value");
					String currency = resultSet.getString("currency");
					String orderStatus = resultSet.getString("order_status");
					String restaurantId = resultSet.getString("restaurant_id");
					String employeeId = resultSet.getString("employee_id");
					String customerId = resultSet.getString("customer_id");

					MonetaryAmount orderTotalValue = Monetary.getDefaultAmountFactory()
							.setCurrency(currency) // Replace "USD" with your actual currency
							.setNumber(orderTotalValueDecimal)
							.create();


					Order order = new Order(orderId, orderTimestamp, orderType, orderTotalValue, currency, orderStatus, restaurantId, employeeId, customerId);
					// Add this HashMap to the customers map using customer_id as the key
					orders.put(orderId, order);
				}
			}
		} catch (SQLException e) {
			System.err.println("Database error in getCurrentOrders: " + e.getMessage());
		}

		return orders;
	}

	private static HashMap<String, Order> getPast30DayOrders() {
		String query = "SELECT * FROM Orders WHERE (order_timestamp >= CURRENT_DATE - INTERVAL '30 days' AND order_timestamp < CURRENT_DATE + INTERVAL '1 day')";
		HashMap<String, Order> orders = new HashMap<>();  // HashMap to store orders by order_id

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			assert DBCredentials.DB_URL.getDBInfo() != null;
			try (Connection connection = DriverManager.getConnection(
					DBCredentials.DB_URL.getDBInfo(),
					DBCredentials.DB_USER.getDBInfo(),
					DBCredentials.DB_PASSWORD.getDBInfo());
			     PreparedStatement updateStatement = connection.prepareStatement(query)) {

				ResultSet resultSet = updateStatement.executeQuery();

				// Loop through the result set and add rows to the HashMap
				while (resultSet.next()) {
					String orderId = resultSet.getString("order_id");
					OffsetDateTime orderTimestamp = resultSet.getObject("order_timestamp", OffsetDateTime.class);
					String orderType = resultSet.getString("order_type");
					BigDecimal orderTotalValueDecimal = resultSet.getBigDecimal("order_total_value");
					String currency = resultSet.getString("currency");
					String orderStatus = resultSet.getString("order_status");
					String restaurantId = resultSet.getString("restaurant_id");
					String employeeId = resultSet.getString("employee_id");
					String customerId = resultSet.getString("customer_id");

					MonetaryAmount orderTotalValue = Monetary.getDefaultAmountFactory()
									.setCurrency(currency) // Replace "USD" with your actual currency
									.setNumber(orderTotalValueDecimal)
									.create();

					Order order = new Order(orderId, orderTimestamp, orderType, orderTotalValue, currency, orderStatus, restaurantId, employeeId, customerId);
					// Add the order to the HashMap using order_id as the key
					orders.put(orderId, order);
				}
			}
		} catch (SQLException e) {
			System.err.println("Database error in getPast30DayOrders: " + e.getMessage());
		}

		return orders;
	}

	public String getCurrency() {
		return currency;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String newOrderStatus) {
		updateOrderStatus(Integer.valueOf(this.ORDER_ID), newOrderStatus);
		this.orderStatus = newOrderStatus;
	}

	public OffsetDateTime getORDER_TIMESTAMP() {
		return ORDER_TIMESTAMP;
	}

	public String getORDER_TYPE() {
		return ORDER_TYPE;
	}

	public String getCUSTOMER_ID() {
		return CUSTOMER_ID;
	}

	public String getRESTAURANT_ID() {
		return RESTAURANT_ID;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		updateOrderEmployeeId(Integer.valueOf(this.ORDER_ID), Integer.valueOf(employeeId));
		this.employeeId = employeeId;
	}
}
