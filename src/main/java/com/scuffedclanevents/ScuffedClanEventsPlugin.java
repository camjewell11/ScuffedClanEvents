package com.scuffedclanevents;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

@Slf4j
@PluginDescriptor(name = "Scuffed Clan Events")
public class ScuffedClanEventsPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ScuffedClanEventsConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private ScuffedClanEventsPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception {
		log.debug("Scuffed Clan Events started!");

		// Create the sidebar panel and navigation button
		panel = new ScuffedClanEventsPanel();
		panel.setOnRefresh(this::loadSheetData);
		final BufferedImage icon = ImageUtil.loadImageResource(ScuffedClanEventsPlugin.class, "icon.png");
		navButton = NavigationButton.builder()
				.tooltip("Scuffed Clan Events")
				.icon(icon)
				.priority(40)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		loadSheetData();
	}

	@Override
	protected void shutDown() throws Exception {
		log.debug("Scuffed Clan Events stopped!");
		clientToolbar.removeNavigation(navButton);
		panel = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			loadSheetData();
		}
	}

	/**
	 * Load data from Google Sheet
	 */
	private void loadSheetData() {
		String sheetId = config.googleSheetId();
		String apiKey = config.googleApiKey();

		if (sheetId.isEmpty() || apiKey.isEmpty()) {
			log.warn("Google Sheet ID or API Key is not configured");
			return;
		}

		try {
			fetchGoogleSheetData(sheetId, apiKey);
		} catch (IOException e) {
			log.error("Failed to fetch Google Sheet data: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetch data from Google Sheets API
	 */
	private void fetchGoogleSheetData(String sheetId, String apiKey) throws IOException {
		String range = "TheHunt!A53";
		String url = "https://sheets.googleapis.com/v4/spreadsheets/" +
				sheetId +
				"/values/" +
				URLEncoder.encode(range, StandardCharsets.UTF_8) +
				"?key=" +
				apiKey;

		try {
			URL urlObj = new URL(url);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("GET");

			Scanner scanner = new Scanner(conn.getInputStream());
			StringBuilder response = new StringBuilder();
			while (scanner.hasNext()) {
				response.append(scanner.nextLine());
			}
			scanner.close();

			log.debug("Sheet data loaded: " + response.toString());
			parseAndDisplaySheetData(response.toString());
		} catch (Exception e) {
			log.error("Error fetching sheet data: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse Google Sheets API response and extract cell A53
	 */
	private void parseAndDisplaySheetData(String jsonResponse) {
		try {
			JSONObject json = new JSONObject(jsonResponse);
			JSONArray values = json.getJSONArray("values");

			String cellA53 = null;
			if (values.length() > 0) {
				JSONArray row = values.getJSONArray(0);
				if (row.length() > 0) {
					cellA53 = row.getString(0);
				}
			}

			if (cellA53 == null || cellA53.isEmpty()) {
				cellA53 = "No data";
			}

			final String display = cellA53;
			if (panel != null) {
				SwingUtilities.invokeLater(() -> panel.setText(display));
			}
			log.debug("Panel updated with A53: " + display);
		} catch (Exception e) {
			log.error("Error parsing sheet data: " + e.getMessage(), e);
		}
	}

	@Provides
	ScuffedClanEventsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ScuffedClanEventsConfig.class);
	}
}
