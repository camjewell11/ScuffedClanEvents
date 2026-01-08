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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.image.BufferedImage;
import java.awt.Color;
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
	private final List<EventEntry> cachedEvents = new ArrayList<>();

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
		String range = "TheHunt!A53:C62";
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

			cachedEvents.clear();

			// Skip header row (index 0)
			for (int i = 1; i < values.length(); i++) {
				JSONArray row = values.optJSONArray(i);
				if (row == null || row.length() == 0) {
					continue;
				}
				String a = row.optString(0, "").trim();
				String b = row.optString(1, "").trim();
				String c = row.optString(2, "").trim();

				if (a.isEmpty() && b.isEmpty() && c.isEmpty()) {
					continue;
				}

				cachedEvents.add(new EventEntry(a, b, c));
			}

			Winner[] winners = new Winner[9];
			List<ScuffedClanEventsPanel.TileData> tiles = buildBingoTiles(winners);
			ScoreResult scores = computeScores(winners);

			if (panel != null) {
				SwingUtilities.invokeLater(() -> {
					panel.setTiles(tiles);
					panel.setTeamAPlaceholder("Red Team points: " + scores.teamA);
					panel.setTeamBPlaceholder("Blue Team points: " + scores.teamB);
				});
			}
			log.debug("Panel updated with bingo board, rows=" + cachedEvents.size() + " scores A=" + scores.teamA
					+ " B=" + scores.teamB);
		} catch (Exception e) {
			log.error("Error parsing sheet data: " + e.getMessage(), e);
		}
	}

	private List<ScuffedClanEventsPanel.TileData> buildBingoTiles(Winner[] winnersOut) {
		List<ScuffedClanEventsPanel.TileData> tiles = new ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			EventEntry e = (i < cachedEvents.size()) ? cachedEvents.get(i) : null;
			if (e == null) {
				tiles.add(new ScuffedClanEventsPanel.TileData("(empty)", BASE_TILE_COLOR));
				winnersOut[i] = Winner.NONE;
				continue;
			}

			Winner w = determineWinner(e);
			winnersOut[i] = w;
			Color color = winnerToColor(w);
			tiles.add(new ScuffedClanEventsPanel.TileData(emptyToPlaceholder(e.a), color));
		}
		return tiles;
	}

	private Winner determineWinner(EventEntry e) {
		boolean isTimeA = isTimeFormat(e.b);
		boolean isTimeB = isTimeFormat(e.c);

		if (isTimeA && isTimeB) {
			double t1 = parseTimeSeconds(e.b);
			double t2 = parseTimeSeconds(e.c);
			if (Double.isNaN(t1) || Double.isNaN(t2)) {
				return Winner.NONE;
			}
			if (t1 < t2)
				return Winner.A;
			if (t2 < t1)
				return Winner.B;
			return Winner.TIE;
		}

		double s1 = parseScore(e.b);
		double s2 = parseScore(e.c);
		if (Double.isNaN(s1) || Double.isNaN(s2)) {
			return Winner.NONE;
		}
		if (s1 > s2)
			return Winner.A;
		if (s2 > s1)
			return Winner.B;
		return Winner.TIE;
	}

	private Color winnerToColor(Winner w) {
		switch (w) {
			case A:
				return TEAM_A_COLOR;
			case B:
				return TEAM_B_COLOR;
			case TIE:
				return TIE_COLOR;
			default:
				return BASE_TILE_COLOR;
		}
	}

	private boolean isTimeFormat(String v) {
		return v != null && v.contains(":");
	}

	private double parseTimeSeconds(String v) {
		if (v == null || v.isEmpty())
			return Double.NaN;
		String[] parts = v.split(":");
		try {
			double seconds = 0;
			for (String part : parts) {
				seconds = seconds * 60 + Double.parseDouble(part.trim());
			}
			return seconds;
		} catch (NumberFormatException ex) {
			return Double.NaN;
		}
	}

	private double parseScore(String v) {
		if (v == null || v.isEmpty())
			return Double.NaN;
		try {
			return Double.parseDouble(v.replace(",", "").trim());
		} catch (NumberFormatException ex) {
			return Double.NaN;
		}
	}

	private static String emptyToPlaceholder(String v) {
		return (v == null || v.isEmpty()) ? "(empty)" : v;
	}

	private ScoreResult computeScores(Winner[] winners) {
		int[][] lines = {
				{ 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, // rows
				{ 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, // cols
				{ 0, 4, 8 }, { 2, 4, 6 } // diags
		};

		// Count completed lines per team and track which tiles are in completed lines
		int completedA = 0, completedB = 0;
		boolean[] inLineA = new boolean[9];
		boolean[] inLineB = new boolean[9];

		for (int[] line : lines) {
			Winner w0 = winners[line[0]];
			if (w0 == Winner.A && winners[line[1]] == Winner.A && winners[line[2]] == Winner.A) {
				completedA++;
				inLineA[line[0]] = true;
				inLineA[line[1]] = true;
				inLineA[line[2]] = true;
			}
			if (w0 == Winner.B && winners[line[1]] == Winner.B && winners[line[2]] == Winner.B) {
				completedB++;
				inLineB[line[0]] = true;
				inLineB[line[1]] = true;
				inLineB[line[2]] = true;
			}
		}

		// Score = 10 per completed line + 1 per tile not in any line
		int scoreA = completedA * 10;
		int scoreB = completedB * 10;

		for (int i = 0; i < 9; i++) {
			if (winners[i] == Winner.A && !inLineA[i])
				scoreA++;
			if (winners[i] == Winner.B && !inLineB[i])
				scoreB++;
		}

		// Full board bonus: if one team owns all 9 tiles, they get 100 instead
		int teamATiles = 0, teamBTiles = 0;
		for (Winner w : winners) {
			if (w == Winner.A)
				teamATiles++;
			else if (w == Winner.B)
				teamBTiles++;
		}

		if (teamATiles == 9)
			scoreA = 100;
		if (teamBTiles == 9)
			scoreB = 100;

		return new ScoreResult(scoreA, scoreB);
	}

	private static final Color TEAM_A_COLOR = new Color(0xD64545); // Red Team
	private static final Color TEAM_B_COLOR = new Color(0x3A7BD5); // Blue Team
	private static final Color TIE_COLOR = new Color(0x777777);
	private static final Color BASE_TILE_COLOR = new Color(50, 45, 38);

	private enum Winner {
		A, B, TIE, NONE
	}

	private static final class ScoreResult {
		final int teamA;
		final int teamB;

		ScoreResult(int teamA, int teamB) {
			this.teamA = teamA;
			this.teamB = teamB;
		}
	}

	private static final class EventEntry {
		private final String a;
		private final String b;
		private final String c;

		EventEntry(String a, String b, String c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

	@Provides
	ScuffedClanEventsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ScuffedClanEventsConfig.class);
	}
}
