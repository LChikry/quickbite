package org.quickbitehub.database;

public abstract class DatabaseOperation {
	protected DatabaseConnection connection;

	abstract void insert();
	abstract void deleteById();
	abstract void updateById();
	abstract void retrieveAllById();
}