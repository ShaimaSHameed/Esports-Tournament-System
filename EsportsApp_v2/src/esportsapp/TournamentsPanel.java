package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class TournamentsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField, startField, endField, prizeField, venueField;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;

    public TournamentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Name", "Start Date", "End Date", "Prize Pool", "Venue ID"};
        model = new DefaultTableModel(cols, 0) {
            @Override
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

        idField = addField(form, g, "Tournament ID:", 0, 0);
        nameField = addField(form, g, "Name:", 0, 2);
        startField = addField(form, g, "Start (YYYY-MM-DD):", 0, 4);
        endField = addField(form, g, "End (YYYY-MM-DD):", 1, 0);
        prizeField = addField(form, g, "Prize Pool:", 1, 2);
        venueField = addField(form, g, "Venue ID:", 1, 4);

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
                idField.setEditable(false);
                nameField.setText(model.getValueAt(row, 1).toString());
                startField.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                endField.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                prizeField.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                venueField.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
            }
        });

        load.addActionListener(e -> loadTournaments());
        insert.addActionListener(e -> {
            if (validateInputs(false)) {
                insertTournament();
            }
        });
        update.addActionListener(e -> {
            if (validateInputs(true)) {
                updateTournament();
            }
        });
        delete.addActionListener(e -> deleteTournament());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadTournaments();
    }

    private boolean validateInputs(boolean isUpdate) {

        // 1. Mandatory checks
        if (idField.getText().trim().isEmpty()
                || nameField.getText().trim().isEmpty()
                || startField.getText().trim().isEmpty()
                || endField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "ID, Name, Start Date, and End Date are mandatory.");
            return false;
        }

        String idStr = idField.getText().trim();
        String prizeStr = prizeField.getText().trim();
        String venueStr = venueField.getText().trim();
        String start = startField.getText().trim();
        String end = endField.getText().trim();

        // 2. Numeric validation using regex (NO exceptions)
        if (!idStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "ID must be numeric.");
            return false;
        }

        if (!prizeStr.isEmpty() && !prizeStr.matches("\\d+(\\.\\d+)?")) {
            JOptionPane.showMessageDialog(this, "Prize must be numeric.");
            return false;
        }

        if (!venueStr.isEmpty() && !venueStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Venue ID must be numeric.");
            return false;
        }

        // 3. Date validation
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}")
                || !end.matches("\\d{4}-\\d{2}-\\d{2}")) {

            JOptionPane.showMessageDialog(this,
                    "Dates must be in YYYY-MM-DD format.");
            return false;
        }

        // 4. Logical date check
        try {
            java.sql.Date startDate = java.sql.Date.valueOf(start);
            java.sql.Date endDate = java.sql.Date.valueOf(end);

            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this,
                        "Start date cannot be after end date.");
                return false;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date values.");
            return false;
        }

        return true;
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

    private void loadTournaments() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM tournament ORDER BY tournament_id")) {
            while (rs.next()) {
                int venueId = rs.getInt(6);
                if (rs.wasNull()) {
                    venueId = -1; // or keep null via Integer
                }

                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getDate(3),
                    rs.getDate(4),
                    rs.getDouble(5),
                    venueId == -1 ? null : venueId
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private boolean hasMatches(int tournamentId) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM match WHERE tournament_id=?")) {

            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return true; // safest fail = block delete
        }
    }

    private void insertTournament() {
        int id = Integer.parseInt(idField.getText().trim());

        if (tournamentExists(id)) {
            JOptionPane.showMessageDialog(this, "Tournament ID already exists!");
            return;
        }
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO tournament VALUES (?,?,TO_DATE(?,'YYYY-MM-DD'),TO_DATE(?,'YYYY-MM-DD'),?,?)")) {
            ps.setInt(1, Integer.parseInt(idField.getText().trim()));
            ps.setString(2, nameField.getText().trim());
            ps.setString(3, startField.getText().trim());
            ps.setString(4, endField.getText().trim());

            if (prizeField.getText().isEmpty()) {
                ps.setNull(5, Types.DOUBLE);
            } else {
                ps.setDouble(5, Double.parseDouble(prizeField.getText().trim()));
            }

            if (venueField.getText().isEmpty()) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, Integer.parseInt(venueField.getText().trim()));
            }

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tournament inserted successfully!");
            loadTournaments();
            clearFields();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 2291) { // Oracle FK violation
                JOptionPane.showMessageDialog(this,
                        "Venue does not exist! Please enter a valid Venue ID.");
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void updateTournament() {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE tournament SET name=?, start_date=TO_DATE(?,'YYYY-MM-DD'), end_date=TO_DATE(?,'YYYY-MM-DD'), prize_pool=?, venue_id=? WHERE tournament_id=?")) {
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, startField.getText().trim());
            ps.setString(3, endField.getText().trim());

            if (prizeField.getText().isEmpty()) {
                ps.setNull(4, Types.DOUBLE);
            } else {
                ps.setDouble(4, Double.parseDouble(prizeField.getText().trim()));
            }

            if (venueField.getText().isEmpty()) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, Integer.parseInt(venueField.getText().trim()));
            }

            ps.setInt(6, Integer.parseInt(idField.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tournament updated successfully!");
            loadTournaments();
            clearFields();
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 2291) { // Oracle FK violation
                JOptionPane.showMessageDialog(this,
                        "Venue does not exist! Please enter a valid Venue ID.");
            } else {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }

    }

    private void deleteTournament() {

        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a tournament first.");
            return;
        }

        int id = Integer.parseInt(idField.getText().trim());

        if (hasMatches(id)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete tournament. Matches are assigned to it.");
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "DELETE FROM tournament WHERE tournament_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deleted successfully!");
            loadTournaments();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private boolean tournamentExists(int id) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM tournament WHERE tournament_id=?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        startField.setText("");
        endField.setText("");
        prizeField.setText("");
        venueField.setText("");
        idField.setEditable(true);
        table.clearSelection();
    }
}
