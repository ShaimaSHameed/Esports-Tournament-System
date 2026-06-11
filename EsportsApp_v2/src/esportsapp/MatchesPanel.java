package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class MatchesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, dateField, mapField, tournamentField, gameField;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;

    public MatchesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Match ID", "Date", "Map", "Tournament ID", "Game ID"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(HEADER_BG, 1));
        scroll.getViewport().setBackground(new Color(155, 180, 210));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_BG, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        idField = addField(form, g, "Match ID:", 0, 0);
        dateField = addField(form, g, "Date (YYYY-MM-DD):", 0, 2);
        mapField = addField(form, g, "Map Name:", 0, 4);
        tournamentField = addField(form, g, "Tournament ID:", 1, 0);
        gameField = addField(form, g, "Game ID:", 1, 2);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnPanel.setBackground(PANEL_BG);
        JButton load = makeButton("Load All", BTN_BLUE);
        JButton insert = makeButton("Insert", BTN_GREEN);
        JButton update = makeButton("Update", ACCENT);
        JButton delete = makeButton("Delete", BTN_RED);
        JButton clear = makeButton("Clear", new Color(100, 100, 120));
        btnPanel.add(load);
        btnPanel.add(insert);
        btnPanel.add(update);
        btnPanel.add(delete);
        btnPanel.add(clear);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 6;
        g.weightx = 0;
        form.add(btnPanel, g);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {

                idField.setText(model.getValueAt(row, 0).toString());
                dateField.setText(model.getValueAt(row, 1).toString());
                mapField.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                tournamentField.setText(model.getValueAt(row, 3).toString());
                gameField.setText(model.getValueAt(row, 4).toString());

              
                setUpdateMode(true);
            }
        });

        load.addActionListener(e -> loadMatches());
        insert.addActionListener(e -> insertMatch());
        update.addActionListener(e -> updateMatch());
        delete.addActionListener(e -> deleteMatch());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadMatches();
    }

    private boolean validateMatchInputs(boolean isUpdate) {

        // 1. Empty check
        if (idField.getText().trim().isEmpty()
                || dateField.getText().trim().isEmpty()
                || tournamentField.getText().trim().isEmpty()
                || gameField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Required fields cannot be empty!");
            return false;
        }

        // 2. Validate numeric IDs
        int matchId, tournamentId, gameId;

        try {
            matchId = Integer.parseInt(idField.getText().trim());
            tournamentId = Integer.parseInt(tournamentField.getText().trim());
            gameId = Integer.parseInt(gameField.getText().trim());

            if (matchId <= 0 || tournamentId <= 0 || gameId <= 0) {
                JOptionPane.showMessageDialog(this, "IDs must be positive numbers!");
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "IDs must be valid numbers!");
            return false;
        }

       
        //  Date format check
        String matchDateStr = dateField.getText().trim();
        if (!matchDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Date must be YYYY-MM-DD");
            return false;
        }

        //  Map length check
        if (mapField.getText().length() > 50) {
            JOptionPane.showMessageDialog(this, "Map name max length is 50");
            return false;
        }

        //  Duplicate Match ID check (ONLY for insert)
        if (!isUpdate && matchIdExists(matchId)) {
            JOptionPane.showMessageDialog(this, "Match ID already exists!");
            return false;
        }

        //  Tournament date range validation
        if (!isMatchDateWithinTournament(tournamentId, matchDateStr)) {
            return false;
        }
        
         //  Check if game exists
        if (!gameExists(gameId)) {
            JOptionPane.showMessageDialog(this, "Game does not exist!");
            return false;
        }


        return true;
    }

    private boolean isMatchDateWithinTournament(int tournamentId, String matchDateStr) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT start_date, end_date FROM tournament WHERE tournament_id = ?")) {

            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Tournament does not exist!");
                return false;
            }

            java.sql.Date start = rs.getDate(1);
            java.sql.Date end = rs.getDate(2);
            java.sql.Date matchDate = java.sql.Date.valueOf(matchDateStr);

            if (start == null || end == null) {
                JOptionPane.showMessageDialog(this,
                        "Tournament does not have valid start/end dates.");
                return false;
            }

            if (matchDate.before(start) || matchDate.after(end)) {
                JOptionPane.showMessageDialog(this,
                        "Match date must be within tournament range:\n"
                        + start + " to " + end);
                return false;
            }

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Date validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean gameExists(int gameId) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM game WHERE game_id = ?")) {

            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            return false; // fail-safe
        }
    }

    private boolean matchIdExists(int matchId) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM match WHERE match_id = ?")) {

            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // true if exists

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking match ID: " + e.getMessage());
            return true;
        }
    }

    private JTextField addField(JPanel p, GridBagConstraints g, String label, int row, int col) {
        g.gridx = col;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        p.add(lbl, g);
        g.gridx = col + 1;
        g.weightx = 1.0;
        JTextField f = new JTextField(12);
        f.setBackground(new Color(170, 195, 220));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setPreferredSize(new Dimension(150, 28));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_BG),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        p.add(f, g);
        return f;
    }

    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(155, 180, 210));
        t.setForeground(TEXT);
        t.setSelectionBackground(new Color(70, 160, 180));
        t.setSelectionForeground(Color.WHITE);
        t.setRowHeight(30);
        t.setFont(new Font("Arial", Font.PLAIN, 13));
        t.setGridColor(new Color(100, 130, 170));
        t.setShowGrid(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Arial", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 35));
    }

    private void loadMatches() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT match_id, match_date, map_name, tournament_id, game_id FROM match ORDER BY match_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getDate(2), rs.getString(3),
                    rs.getInt(4), rs.getInt(5)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertMatch() {

        if (!validateMatchInputs(false)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "INSERT INTO match VALUES (?,TO_DATE(?,'YYYY-MM-DD'),?,?,?)")) {

            ps.setInt(1, Integer.parseInt(idField.getText().trim()));
            ps.setString(2, dateField.getText().trim());
            ps.setString(3, mapField.getText().trim());
            ps.setInt(4, Integer.parseInt(tournamentField.getText().trim()));
            ps.setInt(5, Integer.parseInt(gameField.getText().trim()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Match inserted successfully!");
            loadMatches();
            clearFields();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database constraint error (FK or PK violation)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateMatch() {

        if (!validateMatchInputs(true)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "UPDATE match SET match_date=TO_DATE(?,'YYYY-MM-DD'), map_name=?, tournament_id=?, game_id=? WHERE match_id=?")) {

            ps.setString(1, dateField.getText().trim());
            ps.setString(2, mapField.getText().trim());
            ps.setInt(3, Integer.parseInt(tournamentField.getText().trim()));
            ps.setInt(4, Integer.parseInt(gameField.getText().trim()));
            ps.setInt(5, Integer.parseInt(idField.getText().trim()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Match updated successfully!");
            loadMatches();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void setUpdateMode(boolean isUpdateMode) {
        idField.setEnabled(!isUpdateMode);
    }

    private void deleteMatch() {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "DELETE FROM match WHERE match_id=?")) {
            ps.setInt(1, Integer.parseInt(idField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Match deleted successfully!");
            loadMatches();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        idField.setText("");
        dateField.setText("");
        mapField.setText("");
        tournamentField.setText("");
        gameField.setText("");

        table.clearSelection();

        // allow insert again
        setUpdateMode(false);
    }
}
