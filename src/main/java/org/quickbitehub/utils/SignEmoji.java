package org.quickbitehub.utils;

public enum SignEmoji {
	RED_CIRCLE("\ud83d\udd34"),
	GREEN_CIRCLE("\ud83d\udfe2"),
	ORANGE_CIRCLE("\ud83d\udfe0"),
	BLUE_CIRCLE("\ud83d\udd35");

	private final String code;

	SignEmoji(String emojiCode) {
		this.code = emojiCode;
	}

	public String getCode() {
		return this.code;
	}
}