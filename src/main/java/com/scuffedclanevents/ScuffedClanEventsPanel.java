package com.scuffedclanevents;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.PluginPanel;

public class ScuffedClanEventsPanel extends PluginPanel {
    private final List<JPanel> tilePanels = new ArrayList<>(9);
    private final List<JLabel> tileLabels = new ArrayList<>(9);
    private Runnable onRefresh;

    private final JLabel headerLabel = new JLabel("Scuffed Clan Bingo");
    private final JLabel teamAPlaceholder = new JLabel("Red Team points: -");
    private final JLabel teamBPlaceholder = new JLabel("Blue Team points: -");

    public ScuffedClanEventsPanel() {
        super(false);
        setLayout(new BorderLayout());
        setBackground(new Color(90, 90, 90));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topBar.add(headerLabel, BorderLayout.CENTER);

        JPanel placeholders = new JPanel(new GridLayout(2, 1));
        placeholders.setOpaque(false);
        placeholders.setBorder(new EmptyBorder(8, 0, 8, 0));
        teamAPlaceholder.setForeground(Color.LIGHT_GRAY);
        teamAPlaceholder.setFont(new Font("Arial", Font.PLAIN, 12));
        teamBPlaceholder.setForeground(Color.LIGHT_GRAY);
        teamBPlaceholder.setFont(new Font("Arial", Font.PLAIN, 12));
        placeholders.add(teamAPlaceholder);
        placeholders.add(teamBPlaceholder);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);
        headerContainer.add(topBar, BorderLayout.NORTH);
        headerContainer.add(placeholders, BorderLayout.SOUTH);

        add(headerContainer, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 8, 8));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        for (int i = 0; i < 9; i++) {
            JPanel tile = new JPanel(new BorderLayout());
            tile.setBackground(new Color(50, 45, 38));
            tile.setBorder(new EmptyBorder(8, 8, 8, 8));
            tile.setPreferredSize(new Dimension(80, 80));
            tile.setMinimumSize(new Dimension(80, 80));
            tile.setMaximumSize(new Dimension(80, 80));

            JLabel label = new JLabel("(empty)");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            tile.add(label, BorderLayout.CENTER);

            tilePanels.add(tile);
            tileLabels.add(label);
            grid.add(tile);
        }

        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        gridContainer.add(grid, BorderLayout.NORTH);
        add(gridContainer, BorderLayout.CENTER);

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

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(refreshButton, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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

    // Placeholder setters for future point calculations
    public void setTeamAPlaceholder(String text) {
        teamAPlaceholder.setText(text);
    }

    public void setTeamBPlaceholder(String text) {
        teamBPlaceholder.setText(text);
    }

    public static class TileData {
        public final String title;
        public final Color color;

        public TileData(String title, Color color) {
            this.title = title;
            this.color = color;
        }
    }
}
