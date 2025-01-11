package org.quickbitehub.communicator;

public enum Emoji {
	RED_CIRCLE("\ud83d\udd34"),
	GREEN_CIRCLE("\ud83d\udfe2"),
	ORANGE_CIRCLE("\ud83d\udfe0"),
	BLUE_CIRCLE("\ud83d\udd35"),
	LEFT_MAGNIFIER("\ud83d\udd0e"),
	RIGHT_MAGNIFIER("\ud83d\udd0d"),
	HAND_WAVING("\ud83d\udc4b"),
	SMILING_FACE("\ud83d\ude01"),
	SAD_FACE("\ud83d\ude1e"),
	YELLOW_STARS("\u2728"),
	SPEECH_BALLOON("\ud83d\udcac"),
	HEART_FIGURE("\u2764️"),
	LEFT_TRANSPARENT_ARROW("\u2190"),
	LOCK_WITH_KEY("\ud83d\udd10"),
	GOLDEN_KEY("\ud83d\udd11"),
	REGISTRATION_PAPER("\ud83d\udccb"),
	PASSWORD_DOT("\u25cf");

	private final String code;

	Emoji(String emojiCode) {
		this.code = emojiCode;
	}

	public String getCode() {
		return this.code;
	}
}