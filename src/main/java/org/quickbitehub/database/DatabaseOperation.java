package org.quickbitehub.database;

import java.util.Map;

public abstract class DatabaseOperation<T, ID> {
	protected DatabaseConnection connection;

	DatabaseOperation(DatabaseConnection connection) {
		this.connection = connection;
	}

	abstract void insert(T entity);
	abstract void deleteById(ID id);
	abstract T getById(ID id);
	abstract Map<ID, T> retrieveAllById();
}