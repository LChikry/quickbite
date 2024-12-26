package org.quickbitehub.authentication;

import io.github.cdimascio.dotenv.Dotenv;

public enum DBCredentials {
	DB_USER("DB_USER"),
	DB_PASSWORD("DB_PASSWORD"),
	DB_URL("DB_URL");

	private final String infoType;

	DBCredentials(String infoType) {
		this.infoType = Dotenv.load().get(infoType);
	}

	public String getDBInfo() {
		return this.infoType;
	}
}