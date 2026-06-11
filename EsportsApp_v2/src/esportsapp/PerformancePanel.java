package esportsapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PerformancePanel extends JPanel {

    private JComboBox<String> playerBox, matchBox;
    private JTextField killsField, deathsField, assistsField;

    private JButton loadBtn, saveBtn;
    private JButton deleteBtn;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);

    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);

    private static final Color TEXT = Color.WHITE;

    private String mode = "INSERT";

    public PerformancePanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---------------- HEADER ----------------
        JLabel title = new JLabel("Performance Stats (Per Match)");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(TEXT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(title);

        add(header, BorderLayout.NORTH);

        // ---------------- FORM ----------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        playerBox = new JComboBox<>();
        matchBox = new JComboBox<>();

        killsField = new JTextField(10);
        deathsField = new JTextField(10);
        assistsField = new JTextField(10);

        styleCombo(playerBox);
        styleCombo(matchBox);
        styleField(killsField);
        styleField(deathsField);
        styleField(assistsField);

        JLabel playerLabel = label("Player:");
        JLabel matchLabel = label("Match:");
        JLabel killsLabel = label("Kills:");
        JLabel deathsLabel = label("Deaths:");
        JLabel assistsLabel = label("Assists:");

        int y = 0;

        g.gridx = 0;
        g.gridy = y;
        form.add(playerLabel, g);
        g.gridx = 1;
        form.add(playerBox, g);

        y++;

        g.gridx = 0;
        g.gridy = y;
        form.add(matchLabel, g);
        g.gridx = 1;
        form.add(matchBox, g);

        y++;

        g.gridx = 0;
        g.gridy = y;
        form.add(killsLabel, g);
        g.gridx = 1;
        form.add(killsField, g);

        y++;

        g.gridx = 0;
        g.gridy = y;
        form.add(deathsLabel, g);
        g.gridx = 1;
        form.add(deathsField, g);

        y++;

        g.gridx = 0;
        g.gridy = y;
        form.add(assistsLabel, g);
        g.gridx = 1;
        form.add(assistsField, g);

        y++;

        // ---------------- BUTTONS ----------------
        loadBtn = button("Load", BTN_BLUE);
        saveBtn = button("Save", BTN_GREEN);
        deleteBtn = button("Delete", new Color(180, 70, 70));
        

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(PANEL_BG);
        btnPanel.add(loadBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(deleteBtn);
        

        g.gridx = 0;
        g.gridy = y;
        g.gridwidth = 2;
        form.add(btnPanel, g);

        add(form, BorderLayout.CENTER);

        // ---------------- EVENTS ----------------
        loadBtn.addActionListener(e -> loadPlayers());
        saveBtn.addActionListener(e -> savePerformance());

        playerBox.addActionListener(e -> loadMatchesForPlayer());
        matchBox.addActionListener(e -> loadPerformance());
        deleteBtn.addActionListener(e -> deletePerformance());

        loadPlayers();
    }
    
    private void deletePerformance() {

    if (playerBox.getSelectedItem() == null || matchBox.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(this, "Select player and match first!");
        return;
    }

    int playerId = getId(playerBox);
    int matchId = getId(matchBox);

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete this performance record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM performs WHERE player_id=? AND match_id=?")) {

        ps.setInt(1, playerId);
        ps.setInt(2, matchId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Performance deleted successfully!");
            killsField.setText("");
            deathsField.setText("");
            assistsField.setText("");
            mode = "INSERT";
        } else {
            JOptionPane.showMessageDialog(this, "No record found to delete.");
        }

    } catch (Exception e) {
        showError(e);
    }
}

    // ---------------- LOAD PLAYERS ----------------
    private void loadPlayers() {

        playerBox.removeAllItems();

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT player_id, first_name, last_name FROM player")) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                playerBox.addItem(rs.getInt(1) + " - "
                        + rs.getString(2) + " " + rs.getString(3));
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    // ---------------- LOAD MATCHES ----------------
    private void loadMatchesForPlayer() {

        matchBox.removeAllItems();

        if (playerBox.getSelectedItem() == null) {
            return;
        }

        int playerId = Integer.parseInt(playerBox.getSelectedItem().toString().split(" - ")[0]);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT m.match_id, m.match_date, m.map_name "
                + "FROM match m "
                + "JOIN plays_in pi ON m.match_id = pi.match_id "
                + "JOIN contract c ON pi.team_id = c.team_id "
                + "WHERE c.player_id = ?")) {

            ps.setInt(1, playerId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                matchBox.addItem(rs.getInt(1) + " - "
                        + rs.getDate(2) + " - "
                        + rs.getString(3));
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    // ---------------- LOAD PERFORMANCE ----------------
    private void loadPerformance() {

        if (playerBox.getSelectedItem() == null || matchBox.getSelectedItem() == null) {
            return;
        }

        int playerId = getId(playerBox);
        int matchId = getId(matchBox);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT kills, deaths, assists FROM performs WHERE player_id=? AND match_id=?")) {

            ps.setInt(1, playerId);
            ps.setInt(2, matchId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                mode = "UPDATE";

                killsField.setText(rs.getString(1));
                deathsField.setText(rs.getString(2));
                assistsField.setText(rs.getString(3));

            } else {
                mode = "INSERT";

                killsField.setText("");
                deathsField.setText("");
                assistsField.setText("");
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    // ---------------- SAVE ----------------
    private void savePerformance() {

        if (playerBox.getSelectedItem() == null || matchBox.getSelectedItem() == null) {
            return;
        }

        int playerId = getId(playerBox);
        int matchId = getId(matchBox);

        try (Connection con = DBConnection.getConnection()) {

            if (mode.equals("INSERT")) {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO performs VALUES (?, ?, ?, ?, ?)");

                ps.setInt(1, playerId);
                ps.setInt(2, matchId);
                ps.setInt(3, Integer.parseInt(killsField.getText()));
                ps.setInt(4, Integer.parseInt(deathsField.getText()));
                ps.setInt(5, Integer.parseInt(assistsField.getText()));

                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Performance INSERTED successfully!");

            } else {

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE performs SET kills=?, deaths=?, assists=? WHERE player_id=? AND match_id=?");

                ps.setInt(1, Integer.parseInt(killsField.getText()));
                ps.setInt(2, Integer.parseInt(deathsField.getText()));
                ps.setInt(3, Integer.parseInt(assistsField.getText()));
                ps.setInt(4, playerId);
                ps.setInt(5, matchId);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Performance UPDATED successfully!");
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    // ---------------- HELPERS ----------------
    private int getId(JComboBox<String> box) {
        return Integer.parseInt(box.getSelectedItem().toString().split(" - ")[0]);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        return l;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);

        b.setBackground(color);
        b.setForeground(Color.WHITE);

        b.setFont(new Font("Arial", Font.BOLD, 12));

        b.setFocusPainted(false);

        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return b;
    }

    private void styleCombo(JComboBox<String> box) {
        box.setBackground(new Color(180, 200, 225));
        box.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(180, 200, 225));
        f.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}
