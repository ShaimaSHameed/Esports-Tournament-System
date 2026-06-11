package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RoundsPanel extends JPanel {

    private JTable matchTable, roundTable;
    private DefaultTableModel matchModel, roundModel;
    private JTextField roundNoField, durationField;
    private JComboBox<String> winnerCombo;
    private JButton addBtn, updateBtn, deleteBtn, clearBtn, refreshBtn;
    private int selectedMatchId = -1;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color TEXT = Color.WHITE;

    public RoundsPanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.setBackground(BG);

        matchModel = new DefaultTableModel(new String[]{"Match ID", "Date", "Map"}, 0);
        matchTable = new JTable(matchModel);
        styleTable(matchTable);
        JScrollPane matchScroll = new JScrollPane(matchTable);
        styleScroll(matchScroll);

        roundModel = new DefaultTableModel(new String[]{"Round No", "Duration", "Winner"}, 0);
        roundTable = new JTable(roundModel);
        styleTable(roundTable);
        JScrollPane roundScroll = new JScrollPane(roundTable);
        styleScroll(roundScroll);

        topPanel.add(matchScroll);
        topPanel.add(roundScroll);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HEADER_BG, 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Round No:"), gbc);
        gbc.gridx = 1;
        roundNoField = createField();
        formPanel.add(roundNoField, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(createLabel("Duration:"), gbc);
        gbc.gridx = 3;
        durationField = createField();
        formPanel.add(durationField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Winner:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        winnerCombo = new JComboBox<>();
        winnerCombo.setBackground(new Color(170, 195, 220));
        winnerCombo.setForeground(new Color(40, 40, 40));
        winnerCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(winnerCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(HEADER_BG);

        refreshBtn = new JButton("Refresh Matches");
        refreshBtn.setBackground(new Color(60, 100, 160));
        styleButton(refreshBtn);

        addBtn = new JButton("Add Round");
        addBtn.setBackground(new Color(60, 140, 100));
        styleButton(addBtn);

        updateBtn = new JButton("Update Round");
        updateBtn.setBackground(new Color(70, 160, 180));
        styleButton(updateBtn);

        deleteBtn = new JButton("Delete Round");
        deleteBtn.setBackground(new Color(180, 70, 70));
        styleButton(deleteBtn);

        clearBtn = new JButton("Clear");
        clearBtn.setBackground(new Color(100, 100, 120));
        styleButton(clearBtn);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        matchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = matchTable.getSelectedRow();
                if (row != -1) {
                    selectedMatchId = (int) matchModel.getValueAt(row, 0);
                    loadRounds(selectedMatchId);
                    loadTeamsForMatch(selectedMatchId);
                }
            }
        });

        roundTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roundTable.getSelectedRow();
                if (row != -1) {
                    roundNoField.setText(roundModel.getValueAt(row, 0).toString());
                    durationField.setText(roundModel.getValueAt(row, 1).toString());
                    String winnerText = roundModel.getValueAt(row, 2).toString();
                    int winnerId = Integer.parseInt(winnerText.split(" - ")[0]);
                    for (int i = 0; i < winnerCombo.getItemCount(); i++) {
                        if (winnerCombo.getItemAt(i).startsWith(winnerId + " -")) {
                            winnerCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });

        addBtn.addActionListener(e -> addRound());
        updateBtn.addActionListener(e -> updateRound());
        deleteBtn.addActionListener(e -> deleteRound());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> refreshMatches());

        add(buttonPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

        loadMatches();
    }

    private JTextField createField() {
        JTextField f = new JTextField(15);
        f.setBackground(new Color(170, 195, 220));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setPreferredSize(new Dimension(150, 28));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HEADER_BG),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return f;
    }

    private void refreshMatches() {
        loadMatches();
        roundModel.setRowCount(0);
        selectedMatchId = -1;
        winnerCombo.removeAllItems();
        clearForm();
        JOptionPane.showMessageDialog(this, "Matches refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadTeamsForMatch(int matchId) {
        winnerCombo.removeAllItems();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT t.team_id, t.team_name FROM team t JOIN plays_in p ON t.team_id = p.team_id WHERE p.match_id = ?")) {
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                winnerCombo.addItem(rs.getInt("team_id") + " - " + rs.getString("team_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading teams: " + e.getMessage());
        }
    }

    private void loadMatches() {
        matchModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT match_id, match_date, map_name FROM match")) {
            while (rs.next()) {
                matchModel.addRow(new Object[]{rs.getInt(1), rs.getDate(2), rs.getString(3)});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading matches: " + e.getMessage());
        }
    }

    private void loadRounds(int matchId) {
        roundModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT r.round_no, r.duration, t.team_id, t.team_name " +
                "FROM round r JOIN team t ON r.winner = t.team_id " +
                "WHERE r.match_id = ? ORDER BY r.round_no")) {
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                roundModel.addRow(new Object[]{
                    rs.getInt("round_no"), rs.getInt("duration"),
                    rs.getInt("team_id") + " - " + rs.getString("team_name")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading rounds: " + e.getMessage());
        }
    }

    private void addRound() {
        if (selectedMatchId == -1) { JOptionPane.showMessageDialog(this, "Select a match first!"); return; }
        if (roundNoField.getText().isEmpty() || durationField.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill all fields!"); return; }
        if (winnerCombo.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Select a winner!"); return; }
        try {
            int roundNo = Integer.parseInt(roundNoField.getText());
            int duration = Integer.parseInt(durationField.getText());
            int winnerId = Integer.parseInt(((String) winnerCombo.getSelectedItem()).split(" - ")[0]);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO round VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, roundNo); ps.setInt(2, duration);
                ps.setInt(3, winnerId); ps.setInt(4, selectedMatchId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Round added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadRounds(selectedMatchId); clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add error: " + e.getMessage());
        }
    }

    private void updateRound() {
        int row = roundTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a round!"); return; }
        try {
            int duration = Integer.parseInt(durationField.getText());
            int winnerId = Integer.parseInt(((String) winnerCombo.getSelectedItem()).split(" - ")[0]);
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                    "UPDATE round SET duration = ?, winner = ? WHERE round_no = ? AND match_id = ?")) {
                ps.setInt(1, duration); ps.setInt(2, winnerId);
                ps.setInt(3, (int) roundModel.getValueAt(row, 0)); ps.setInt(4, selectedMatchId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Round updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadRounds(selectedMatchId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update error: " + e.getMessage());
        }
    }

    private void deleteRound() {
        int row = roundTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a round!"); return; }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM round WHERE round_no = ? AND match_id = ?")) {
            ps.setInt(1, (int) roundModel.getValueAt(row, 0)); ps.setInt(2, selectedMatchId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Round deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRounds(selectedMatchId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage());
        }
    }

    private void clearForm() {
        roundNoField.setText("");
        durationField.setText("");
        winnerCombo.setSelectedIndex(-1);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private void styleButton(JButton btn) {
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(155, 180, 210));
        t.setForeground(TEXT);
        t.setSelectionBackground(new Color(70, 160, 180));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(28);
        t.setFont(new Font("Arial", Font.PLAIN, 13));
        t.setGridColor(new Color(100, 130, 170));
        t.setShowGrid(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Arial", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 35));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(HEADER_BG, 1));
        sp.getViewport().setBackground(new Color(155, 180, 210));
    }
}