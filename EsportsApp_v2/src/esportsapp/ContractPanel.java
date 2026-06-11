package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ContractPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField playerIdField, teamIdField;
    private int selectedPlayerId = -1;
    private int selectedTeamId = -1;
    private boolean insertMode = true;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;

    public ContractPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Player ID", "First Name", "Last Name", "Team ID", "Team Name"};
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

        playerIdField = addField(form, g, "Player ID:", 0, 0);
        teamIdField = addField(form, g, "Team ID:", 0, 2);

        playerIdField.setEditable(false);
        teamIdField.setEditable(false);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnPanel.setBackground(PANEL_BG);
        JButton load = makeButton("Load All", BTN_BLUE);
        JButton insert = makeButton("Assign", BTN_GREEN);
        JButton delete = makeButton("Remove", BTN_RED);
        JButton clear = makeButton("Clear", new Color(100, 100, 120));
        btnPanel.add(load);
        btnPanel.add(insert);
        btnPanel.add(delete);
        btnPanel.add(clear);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 4;
        g.weightx = 0;
        form.add(btnPanel, g);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();

            if (row >= 0) {

                // VIEW MODE
                insertMode = false;

                selectedPlayerId = Integer.parseInt(model.getValueAt(row, 0).toString());
                selectedTeamId = Integer.parseInt(model.getValueAt(row, 3).toString());

                playerIdField.setText(String.valueOf(selectedPlayerId));
                teamIdField.setText(String.valueOf(selectedTeamId));

                // lock fields
                playerIdField.setEditable(false);
                teamIdField.setEditable(false);

            } else {

                // INSERT MODE
                switchToInsertMode();
            }
        });

        load.addActionListener(e -> loadContracts());
        insert.addActionListener(e -> insertContract());
        delete.addActionListener(e -> deleteContract());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadContracts();
    }

    private void switchToInsertMode() {
        insertMode = true;

        selectedPlayerId = -1;
        selectedTeamId = -1;

        playerIdField.setText("");
        teamIdField.setText("");

        playerIdField.setEditable(true);
        teamIdField.setEditable(true);
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

    private void loadContracts() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT p.player_id, p.first_name, p.last_name, t.team_id, t.team_name "
                + "FROM contract c "
                + "JOIN player p ON c.player_id = p.player_id "
                + "JOIN team t ON c.team_id = t.team_id "
                + "ORDER BY t.team_name, p.last_name")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getInt(4), rs.getString(5)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertContract() {

        String pText = playerIdField.getText().trim();
        String tText = teamIdField.getText().trim();

        // 1. EMPTY CHECK
        if (pText.isEmpty() || tText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Player ID and Team ID are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // 2. NUMBER VALIDATION
            int playerId;
            int teamId;

            try {
                playerId = Integer.parseInt(pText);
                teamId = Integer.parseInt(tText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "IDs must be valid numbers!");
                return;
            }

            // 3. CHECK PLAYER EXISTS
            PreparedStatement pCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM player WHERE player_id=?"
            );
            pCheck.setInt(1, playerId);
            ResultSet pr = pCheck.executeQuery();
            pr.next();

            if (pr.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this, "Player ID does not exist!");
                return;
            }

            // 4. CHECK TEAM EXISTS
            PreparedStatement tCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM team WHERE team_id=?"
            );
            tCheck.setInt(1, teamId);
            ResultSet tr = tCheck.executeQuery();
            tr.next();

            if (tr.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this, "Team ID does not exist!");
                return;
            }

            // 5. CHECK EXISTING CONTRACT
            PreparedStatement check = con.prepareStatement(
                    "SELECT COUNT(*) FROM contract WHERE player_id=?"
            );
            check.setInt(1, playerId);

            ResultSet rs = check.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Player already assigned. Replace?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                PreparedStatement update = con.prepareStatement(
                        "UPDATE contract SET team_id=? WHERE player_id=?"
                );
                update.setInt(1, teamId);
                update.setInt(2, playerId);
                update.executeUpdate();

            } else {

                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO contract VALUES (?,?)"
                );
                insert.setInt(1, playerId);
                insert.setInt(2, teamId);
                insert.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Contract saved successfully!");
            loadContracts();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteContract() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contract first!");
            return;
        }

        int playerId = Integer.parseInt(model.getValueAt(row, 0).toString());
        int teamId = Integer.parseInt(model.getValueAt(row, 3).toString());

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "DELETE FROM contract WHERE player_id=? AND team_id=?")) {

            ps.setInt(1, playerId);
            ps.setInt(2, teamId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Contract removed successfully!");
            loadContracts();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        switchToInsertMode();
        table.clearSelection();

    }
}
