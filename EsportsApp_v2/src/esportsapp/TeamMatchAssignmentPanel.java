package esportsapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TeamMatchAssignmentPanel extends JPanel {

    private JComboBox<String> teamBox, matchBox;
    private JButton assignBtn, loadBtn;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_RED = new Color(200, 80, 80);    // warning / delete
    private static final Color TEXT = Color.WHITE;

    public TeamMatchAssignmentPanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---------------- HEADER ----------------
        JLabel title = new JLabel("Assign Teams to Matches");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(TEXT);

        JPanel header = new JPanel();
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.add(title);

        add(header, BorderLayout.NORTH);

        // ---------------- FORM PANEL ----------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        teamBox = new JComboBox<>();
        matchBox = new JComboBox<>();

        JLabel teamLabel = new JLabel("Select Team:");
        JLabel matchLabel = new JLabel("Select Match:");

        styleLabel(teamLabel);
        styleLabel(matchLabel);

        // Team
        g.gridx = 0;
        g.gridy = 0;
        form.add(teamLabel, g);

        g.gridx = 1;
        teamBox.setPreferredSize(new Dimension(200, 28));
        form.add(teamBox, g);

        // Match
        g.gridx = 0;
        g.gridy = 1;
        form.add(matchLabel, g);

        g.gridx = 1;
        matchBox.setPreferredSize(new Dimension(200, 28));
        form.add(matchBox, g);

        // ---------------- BUTTONS ----------------
        assignBtn = makeButton("Assign", BTN_GREEN);
        loadBtn = makeButton("Load Data", BTN_RED);
        JButton removeBtn = makeButton("Remove", BTN_RED);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(PANEL_BG);
        btnPanel.add(loadBtn);
        btnPanel.add(assignBtn);
        btnPanel.add(removeBtn);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        form.add(btnPanel, g);

        add(form, BorderLayout.CENTER);

        // ---------------- ACTIONS ----------------
        loadBtn.addActionListener(e -> loadData());
        assignBtn.addActionListener(e -> assignTeamToMatch());
        teamBox.addActionListener(e -> loadMatchesForSelectedTeam());
        removeBtn.addActionListener(e -> removeAssignment());

        loadData();
    }

    private void removeAssignment() {

        if (teamBox.getSelectedItem() == null || matchBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Select team and match.");
            return;
        }

        String selected = matchBox.getSelectedItem().toString();

      
        if (!selected.contains("✔")) {
            JOptionPane.showMessageDialog(this, "This match is not assigned to the team!");
            return;
        }

        int teamId = Integer.parseInt(teamBox.getSelectedItem().toString().split(" - ")[0]);
        int matchId = Integer.parseInt(selected.split(" - ")[0]);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "DELETE FROM plays_in WHERE team_id=? AND match_id=?")) {

            ps.setInt(1, teamId);
            ps.setInt(2, matchId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Assignment removed!");

            loadMatchesForSelectedTeam(); // refresh

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadMatchesForSelectedTeam() {

        matchBox.removeAllItems();

        if (teamBox.getSelectedItem() == null) {
            return;
        }

        int teamId = Integer.parseInt(teamBox.getSelectedItem().toString().split(" - ")[0]);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT m.match_id, m.match_date, m.map_name "
                    + "FROM match m "
                    + "WHERE m.tournament_id IN ("
                    + "SELECT tournament_id FROM registers WHERE team_id = ?)"
            );

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int matchId = rs.getInt("match_id");

                // 🔍 check if assigned
                PreparedStatement check = con.prepareStatement(
                        "SELECT 1 FROM plays_in WHERE team_id=? AND match_id=?"
                );
                check.setInt(1, teamId);
                check.setInt(2, matchId);

                ResultSet rs2 = check.executeQuery();

                boolean assigned = rs2.next();

                String item = matchId + " - "
                        + rs.getDate("match_date") + " - "
                        + rs.getString("map_name");

                if (assigned) {
                    item += " ✔";
                }

                matchBox.addItem(item);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading matches: " + ex.getMessage());
        }
    }

    // ---------------- LOAD DATA ----------------
    private void loadData() {

        teamBox.removeAllItems();
        matchBox.removeAllItems();

        try (Connection con = DBConnection.getConnection()) {

            // load teams
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT team_id, team_name FROM team ORDER BY team_id");
            ResultSet rs1 = ps1.executeQuery();

            while (rs1.next()) {
                teamBox.addItem(rs1.getInt(1) + " - " + rs1.getString(2));
            }

            // load matches
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT match_id, match_date FROM match ORDER BY match_id");
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                matchBox.addItem(rs2.getInt(1) + " - " + rs2.getDate(2));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load Error: " + ex.getMessage());
        }
    }

    // ---------------- ASSIGN ----------------
    private void assignTeamToMatch() {

        if (teamBox.getSelectedItem() == null || matchBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select both team and match.");
            return;
        }

        int teamId = Integer.parseInt(teamBox.getSelectedItem().toString().split(" - ")[0]);
        int matchId = Integer.parseInt(matchBox.getSelectedItem().toString().split(" - ")[0]);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "INSERT INTO plays_in VALUES (?, ?)")) {

            ps.setInt(1, teamId);
            ps.setInt(2, matchId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Team assigned to match successfully!");

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "This team is already assigned to this match!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ---------------- UI HELPERS ----------------
    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        return b;
    }

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
    }
}
