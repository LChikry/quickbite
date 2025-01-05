package org.quickbitehub.communicator;

public enum TimeConstants {
	SHORT_DELAY_TIME_SEC(8), // in seconds
	STANDARD_DELAY_TIME_SEC(30), // in seconds
	LONG_DELAY_TIME_SEC(90), // in seconds
	LARGE_DELAY_TIME_SEC(180), // in seconds
	NO_DELAY_TIME(0); // in seconds

	private final long timeInSeconds;
	TimeConstants(long timeInSeconds) {
		this.timeInSeconds = timeInSeconds;
	}
	public long time() {return timeInSeconds;}
}
