package com.scuffedclanevents;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import net.runelite.client.ui.PluginPanel;

public class ScuffedClanEventsPanel extends PluginPanel {
    private JLabel textLabel;
    private Runnable onRefresh;

    public ScuffedClanEventsPanel() {
        super(false);
        setLayout(new BorderLayout());
        setBackground(new Color(70, 61, 50));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

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
        topBar.add(refreshButton, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        textLabel = new JLabel("Loading data...");
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        add(textLabel, BorderLayout.CENTER);
    }

    public void setText(String text) {
        textLabel.setText(text);
        repaint();
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }
}
