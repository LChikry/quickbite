package org.quickbitehub.database;

import java.sql.Connection;

public interface DatabaseConnection {
	Connection getDbConnection();
	boolean closeDbConnection();
}