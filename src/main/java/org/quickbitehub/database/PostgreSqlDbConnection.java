package org.quickbitehub.database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum PostgreSqlDbConnection implements DatabaseConnection{
	INSTANCE;
	private Connection connection;

	@Override
	public Connection getDbConnection() {
		if (connection != null)	return connection;
		try {
			Class.forName("org.postgresql.Driver");
			String DB_USER = Dotenv.load().get("DB_USER");
			String DB_PASSWORD = Dotenv.load().get("DB_PASSWORD");
			String DB_URL = Dotenv.load().get("DB_URL");
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			return connection;
		} catch (SQLException e) {
			System.out.println("DatabaseOperation Connection is Failed to be Established");
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			System.out.println("PostgreSQL Driver Class is not Found");
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean closeDbConnection() {
		if (connection == null) return true;
		try {
			if (connection.isClosed()) return true;
			connection.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}