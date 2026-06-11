package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class TeamCoachAssignPanel extends JPanel {

    private JTable teamTable, coachTable;
    private DefaultTableModel teamModel, coachModel;

    private JTextField idField, nameField, salaryField;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color TABLE_BG = new Color(155, 180, 210);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color TEXT = Color.WHITE;

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        return lbl;
    }

    private JTextField styleField(JTextField field, int width) {
        field.setPreferredSize(new Dimension(width, 28));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBackground(new Color(170, 195, 220));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 100, 140)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return field;
    }

    private class CoachInput {

        int coachId;
        String name;
        Double salary; // nullable

        CoachInput(int coachId, String name, Double salary) {
            this.coachId = coachId;
            this.name = name;
            this.salary = salary;
        }
    }

    private CoachInput validateInput(boolean requireId) {

        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String salaryText = salaryField.getText().trim();

        // ===== REQUIRED CHECKS =====
        if (requireId && idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Coach ID is required!");
            return null;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required!");
            return null;
        }

        int coachId = 0;

        // ===== ID VALIDATION =====
        if (requireId) {
            try {
                coachId = Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Coach ID must be a number!");
                return null;
            }
        }

        // ===== SALARY VALIDATION =====
        Double salary = null;

        if (!salaryText.isEmpty()) {
            try {
                salary = Double.parseDouble(salaryText);

                if (salary < 0) {
                    JOptionPane.showMessageDialog(this, "Salary cannot be negative!");
                    return null;
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Salary must be a valid number!");
                return null;
            }
        }

        return new CoachInput(coachId, name, salary);
    }

    public TeamCoachAssignPanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);

        // ===== TABLES =====
        teamModel = new DefaultTableModel(new String[]{"Team ID", "Team Name"}, 0);
        teamTable = new JTable(teamModel);
        styleTable(teamTable);

        coachModel = new DefaultTableModel(new String[]{"Coach ID", "Name", "Salary"}, 0);
        coachTable = new JTable(coachModel);
        styleTable(coachTable);

        JScrollPane sp1 = new JScrollPane(teamTable);
        JScrollPane sp2 = new JScrollPane(coachTable);
        styleScroll(sp1);
        styleScroll(sp2);

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 10));
        top.setBackground(PANEL_BG);
        top.add(sp1);
        top.add(sp2);

        // ===== FORM =====
        idField = new JTextField();
        nameField = new JTextField();
        salaryField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

// ===== ROW 1 =====
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(createLabel("Coach ID"), gbc);

        gbc.gridx = 1;
        form.add(styleField(idField, 150), gbc);

// ===== ROW 2 =====
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(createLabel("Name"), gbc);

        gbc.gridx = 1;
        form.add(styleField(nameField, 150), gbc);

// ===== ROW 3 =====
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(createLabel("Salary"), gbc);

        gbc.gridx = 1;
        form.add(styleField(salaryField, 150), gbc);

        // ===== BUTTONS =====
        JButton addBtn = new JButton("Assign / Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh Teams");
        styleButton(refreshBtn, new Color(90, 120, 180));

        refreshBtn.addActionListener(e -> loadTeams());
        

        styleButton(addBtn, new Color(60, 140, 100));      // green
        styleButton(updateBtn, new Color(70, 160, 180));    // teal
        styleButton(deleteBtn, new Color(180, 70, 70));     // red
        styleButton(clearBtn, new Color(100, 100, 120));    // grey

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(PANEL_BG);
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(refreshBtn);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(PANEL_BG);
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.SOUTH);

        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // ===== EVENTS =====
        teamTable.getSelectionModel().addListSelectionListener(e -> loadCoachForTeam());

        addBtn.addActionListener(e -> addCoach());
        updateBtn.addActionListener(e -> updateCoach());
        deleteBtn.addActionListener(e -> deleteCoach());
        clearBtn.addActionListener(e -> clearForm());

        loadTeams();
    }

    // ===== LOAD =====
    private void loadTeams() {
        teamModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT team_id, team_name FROM team");

            while (rs.next()) {
                teamModel.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2)
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadCoachForTeam() {

        coachModel.setRowCount(0);

        int row = teamTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        int teamId = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT coach_id, name, salary FROM coach WHERE team_id=?"
            );
            ps.setInt(1, teamId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                coachModel.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getDouble(3)
                });

                idField.setText(rs.getString(1));
                idField.setEditable(false); // cannot change ID
                nameField.setText(rs.getString(2));
                salaryField.setText(rs.getString(3));
            } else {
                clearForm();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== ACTIONS =====
    private void addCoach() {

        CoachInput input = validateInput(true);
        if (input == null) {
            return;
        }

        int row = teamTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a team first!");
            return;
        }

        int teamId = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            // ===== CHECK DUPLICATE COACH ID (PRIMARY KEY) =====
            PreparedStatement checkId = con.prepareStatement(
                    "SELECT COUNT(*) FROM coach WHERE coach_id=?"
            );
            checkId.setInt(1, input.coachId);

            ResultSet rsId = checkId.executeQuery();
            rsId.next();

            if (rsId.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Coach ID already exists! Please use a different ID.");
                return;
            }

            // ===== CHECK IF TEAM ALREADY HAS A COACH =====
            PreparedStatement checkTeam = con.prepareStatement(
                    "SELECT COUNT(*) FROM coach WHERE team_id=?"
            );
            checkTeam.setInt(1, teamId);

            ResultSet rs = checkTeam.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "This team already has a coach. Replace it?",
                        "Confirm Replace",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                PreparedStatement del = con.prepareStatement(
                        "DELETE FROM coach WHERE team_id=?"
                );
                del.setInt(1, teamId);
                del.executeUpdate();
            }

            // ===== INSERT COACH =====
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO coach (coach_id, name, salary, team_id) VALUES (?, ?, ?, ?)"
            );

            ps.setInt(1, input.coachId);
            ps.setString(2, input.name);

            if (input.salary == null) {
                ps.setNull(3, java.sql.Types.NUMERIC);
            } else {
                ps.setDouble(3, input.salary);
            }

            ps.setInt(4, teamId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Coach assigned successfully!");

            loadCoachForTeam();

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this,
                    "Database constraint violation (duplicate or invalid data).");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage());
        }
    }

    private void updateCoach() {
        CoachInput input = validateInput(false);
        if (input == null) {
            return;
        }

        int row = teamTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        int teamId = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE coach SET name=?, salary=? WHERE team_id=?"
            );

            ps.setString(1, input.name);

            if (input.salary == null) {
                ps.setNull(2, java.sql.Types.DOUBLE);
            } else {
                ps.setDouble(2, input.salary);
            }
            ps.setInt(3, teamId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Updated!");
            } else {
                JOptionPane.showMessageDialog(this, "No coach found!");
            }

            loadCoachForTeam();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void deleteCoach() {

        int row = teamTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        int teamId = (int) teamModel.getValueAt(row, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM coach WHERE team_id=?"
            );
            ps.setInt(1, teamId);

            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "Coach removed!");
            } else {
                JOptionPane.showMessageDialog(this, "No coach to delete!");
            }

            loadCoachForTeam();
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        salaryField.setText("");
        idField.setEditable(true);
    }

    // ===== STYLE =====
    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG);
        t.setForeground(TEXT);

        t.setSelectionBackground(new Color(70, 160, 180)); // accent
        t.setSelectionForeground(Color.WHITE);

        t.setRowHeight(30);
        t.setFont(new Font("Arial", Font.PLAIN, 13));

        t.setGridColor(new Color(100, 130, 170));
        t.setShowGrid(true);

        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Arial", Font.BOLD, 13));

        h.setPreferredSize(new Dimension(0, 35));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(new Color(70, 100, 140), 1));
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBackground(TABLE_BG);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }
}
