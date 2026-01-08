package com.scuffedclanevents;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.PluginPanel;

public class ScuffedClanEventsPanel extends PluginPanel {
    private final List<JPanel> tilePanels = new ArrayList<>(9);
    private final List<JLabel> tileLabels = new ArrayList<>(9);
    private final List<JLabel> highscoreTitleLabels = new ArrayList<>(9);
    private final List<JLabel> highscoreValueLabels = new ArrayList<>(9);
    private Runnable onRefresh;

    private final JLabel headerLabel = new JLabel("Scuffed Clan Bingo");
    private final JLabel teamAPlaceholder = new JLabel("Red Team points: -");
    private final JLabel teamBPlaceholder = new JLabel("Blue Team points: -");
    private final JLabel lastUpdatedLabel = new JLabel("Last updated: -");
    private final JButton linkButton1 = new JButton("Punch Card Board");
    private final JButton linkButton2 = new JButton("WiseOldMan Tracking");

    public ScuffedClanEventsPanel() {
        super(false);
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top section: Header, Scoring, Last Updated
        JPanel topContent = new JPanel();
        topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
        topContent.setOpaque(false);
        topContent.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContent.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        topBar.add(headerLabel, BorderLayout.CENTER);
        topContent.add(topBar);

        // Scoring header
        JLabel scoringHeader = new JLabel("Scoring");
        scoringHeader.setForeground(Color.WHITE);
        scoringHeader.setFont(new Font("Arial", Font.BOLD, 12));
        scoringHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoringHeader.setHorizontalAlignment(JLabel.CENTER);
        scoringHeader.setBorder(new EmptyBorder(8, 0, 4, 0));
        topContent.add(scoringHeader);

        // Team placeholders
        JPanel placeholders = new JPanel(new GridLayout(2, 1));
        placeholders.setOpaque(false);
        placeholders.setBorder(new EmptyBorder(4, 0, 8, 0));
        placeholders.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        placeholders.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamAPlaceholder.setForeground(Color.LIGHT_GRAY);
        teamAPlaceholder.setFont(new Font("Arial", Font.PLAIN, 12));
        teamBPlaceholder.setForeground(Color.LIGHT_GRAY);
        teamBPlaceholder.setFont(new Font("Arial", Font.PLAIN, 12));
        placeholders.add(teamAPlaceholder);
        placeholders.add(teamBPlaceholder);
        topContent.add(placeholders);

        // Last updated label
        lastUpdatedLabel.setForeground(Color.LIGHT_GRAY);
        lastUpdatedLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        lastUpdatedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lastUpdatedLabel.setHorizontalAlignment(JLabel.CENTER);
        lastUpdatedLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        topContent.add(lastUpdatedLabel);

        // Links header
        JLabel linksHeader = new JLabel("Links");
        linksHeader.setForeground(Color.WHITE);
        linksHeader.setFont(new Font("Arial", Font.BOLD, 12));
        linksHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        linksHeader.setHorizontalAlignment(JLabel.CENTER);
        linksHeader.setBorder(new EmptyBorder(4, 0, 4, 0));
        topContent.add(linksHeader);

        // Link buttons
        JPanel linksPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        linksPanel.setOpaque(false);
        linksPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        linkButton1.addActionListener(e -> openLink(getLinkUrl(0)));
        linkButton2.addActionListener(e -> openLink(getLinkUrl(1)));
        linksPanel.add(linkButton1);
        linksPanel.add(linkButton2);
        topContent.add(linksPanel);

        add(topContent, BorderLayout.NORTH);

        // Center section: Bingo grid + Highscores
        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);
        centerStack.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel boardHeader = new JLabel("Board");
        boardHeader.setForeground(Color.WHITE);
        boardHeader.setFont(new Font("Arial", Font.BOLD, 12));
        boardHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        boardHeader.setHorizontalAlignment(JLabel.CENTER);
        boardHeader.setBorder(new EmptyBorder(8, 0, 4, 0));
        centerStack.add(boardHeader);

        JPanel grid = new JPanel(new GridLayout(3, 3, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);
        grid.setPreferredSize(new Dimension(240, 240));
        grid.setMaximumSize(new Dimension(240, 240));
        grid.setMinimumSize(new Dimension(240, 240));
        for (int i = 0; i < 9; i++) {
            JPanel tile = new JPanel(new BorderLayout());
            tile.setBackground(new Color(50, 45, 38));
            tile.setBorder(new EmptyBorder(8, 8, 8, 8));
            tile.setPreferredSize(new Dimension(70, 70));
            tile.setMinimumSize(new Dimension(70, 70));
            tile.setMaximumSize(new Dimension(70, 70));

            JLabel label = new JLabel("(empty)");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            tile.add(label, BorderLayout.CENTER);

            tilePanels.add(tile);
            tileLabels.add(label);
            grid.add(tile);
        }
        centerStack.add(grid);
        centerStack.add(Box.createVerticalStrut(10));

        JLabel highscoresHeader = new JLabel("Tile Highscores");
        highscoresHeader.setForeground(Color.WHITE);
        highscoresHeader.setFont(new Font("Arial", Font.BOLD, 12));
        highscoresHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        highscoresHeader.setHorizontalAlignment(JLabel.CENTER);
        highscoresHeader.setBorder(new EmptyBorder(8, 0, 4, 0));
        centerStack.add(highscoresHeader);

        JPanel highscoresPanel = new JPanel(new GridLayout(9, 2, 4, 4));
        highscoresPanel.setOpaque(false);
        highscoresPanel.setMaximumSize(new Dimension(240, 9 * 20 + 16));
        highscoresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (int i = 0; i < 9; i++) {
            JLabel title = new JLabel("(empty)");
            title.setForeground(Color.LIGHT_GRAY);
            title.setFont(new Font("Arial", Font.PLAIN, 12));
            JLabel value = new JLabel("-");
            value.setForeground(Color.LIGHT_GRAY);
            value.setFont(new Font("Arial", Font.PLAIN, 12));
            highscoreTitleLabels.add(title);
            highscoreValueLabels.add(value);
            highscoresPanel.add(title);
            highscoresPanel.add(value);
        }
        centerStack.add(highscoresPanel);

        add(centerStack, BorderLayout.CENTER);

        // Bottom section: Refresh only
        JPanel bottomContent = new JPanel(new BorderLayout());
        bottomContent.setOpaque(false);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            if (onRefresh != null) {
                refreshButton.setEnabled(false);
                try {
                    onRefresh.run();
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        });

        JPanel refreshPanel = new JPanel(new BorderLayout());
        refreshPanel.setOpaque(false);
        refreshPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        refreshPanel.add(refreshButton, BorderLayout.CENTER);
        bottomContent.add(refreshPanel, BorderLayout.SOUTH);

        add(bottomContent, BorderLayout.SOUTH);
    }

    private String linkUrl1 = "";
    private String linkUrl2 = "";

    public void setLinkUrls(String url1, String url2) {
        this.linkUrl1 = url1 != null ? url1 : "";
        this.linkUrl2 = url2 != null ? url2 : "";
        linkButton1.setEnabled(!linkUrl1.isEmpty());
        linkButton2.setEnabled(!linkUrl2.isEmpty());
    }

    private String getLinkUrl(int index) {
        return index == 0 ? linkUrl1 : linkUrl2;
    }

    private void openLink(String url) {
        if (url == null || url.isEmpty())
            return;
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTiles(List<TileData> tiles) {
        for (int i = 0; i < tilePanels.size(); i++) {
            TileData data = (i < tiles.size()) ? tiles.get(i) : null;
            JPanel tile = tilePanels.get(i);
            JLabel label = tileLabels.get(i);

            if (data == null) {
                label.setText("(empty)");
                tile.setBackground(new Color(50, 45, 38));
            } else {
                label.setText(data.title);
                tile.setBackground(data.color != null ? data.color : new Color(50, 45, 38));
            }
        }
        repaint();
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    public void setTeamAPlaceholder(String text) {
        teamAPlaceholder.setText(text);
    }

    public void setTeamBPlaceholder(String text) {
        teamBPlaceholder.setText(text);
    }

    public void setLastUpdated(String text) {
        lastUpdatedLabel.setText(text);
    }

    public void setHighscores(List<HighscoreRow> rows) {
        for (int i = 0; i < highscoreTitleLabels.size(); i++) {
            HighscoreRow row = (i < rows.size()) ? rows.get(i) : null;
            JLabel titleLabel = highscoreTitleLabels.get(i);
            JLabel valueLabel = highscoreValueLabels.get(i);
            if (row == null) {
                titleLabel.setText("(empty)");
                titleLabel.setForeground(Color.LIGHT_GRAY);
                valueLabel.setText("-");
                valueLabel.setForeground(Color.LIGHT_GRAY);
            } else {
                titleLabel.setText(row.title);
                titleLabel.setForeground(Color.LIGHT_GRAY);
                valueLabel.setText(row.value);
                valueLabel.setForeground(row.color != null ? row.color : Color.LIGHT_GRAY);
            }
        }
        repaint();
    }

    public static class TileData {
        public final String title;
        public final Color color;

        public TileData(String title, Color color) {
            this.title = title;
            this.color = color;
        }
    }

    public static class HighscoreRow {
        public final String title;
        public final String value;
        public final Color color;

        public HighscoreRow(String title, String value, Color color) {
            this.title = title;
            this.value = value;
            this.color = color;
        }
    }
}
