package com.scuffedclanevents;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("scuffedclanevents")
public interface ScuffedClanEventsConfig extends Config {
	@ConfigItem(keyName = "googleSheetId", name = "Google Sheet ID", description = "The ID of the Google Sheet to use")
	default String googleSheetId() {
		return "";
	}

	@ConfigItem(keyName = "googleApiKey", name = "Google API Key", description = "Your Google API key for accessing the sheet")
	default String googleApiKey() {
		return "";
	}
}
