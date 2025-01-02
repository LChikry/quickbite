package org.quickbitehub.consumer;

import org.intellij.lang.annotations.Language;

public enum LanguageType {
	ARABIC("Arabic"),
	ENGLISH("English"),
	FRENCH("Français");

	private final String languageName;
	LanguageType(String name) {
		languageName = name;
	}

	public String getName() {
		return languageName;
	}
}
