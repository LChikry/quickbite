package org.quickbitehub.order;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Order {
	private String orderId;
	private LocalDateTime orderTimestmap;
	private OrderType orderType;
	private Double orderTotalValue;
	private String currency = "MAD";
	private OrderStatus orderStatus;
	private String restaurantId;
	private String employeeId;
	private String customerId;

	static public HashMap<String, Order> allOrders; // orderId -> Order



}
