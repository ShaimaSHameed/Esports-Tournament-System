package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RegistrationPanel extends JPanel {

    private JTable teamTable, tournamentTable, registerTable;
    private DefaultTableModel teamModel, tournamentModel, registerModel;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color TEXT = Color.WHITE;

    public RegistrationPanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---------------- TOP SPLIT ----------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setBackground(BG);

        // TEAM TABLE
        teamModel = new DefaultTableModel(new String[]{"Team ID", "Team Name"}, 0);
        teamTable = new JTable(teamModel);
        styleTable(teamTable);
        JScrollPane teamScroll = new JScrollPane(teamTable);
        styleScroll(teamScroll);

        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBackground(PANEL_BG);
        teamPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HEADER_BG, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        JLabel teamLabel = new JLabel("  Select a Team", SwingConstants.LEFT);
        teamLabel.setFont(new Font("Arial", Font.BOLD, 13));
        teamLabel.setForeground(TEXT);
        teamLabel.setBackground(HEADER_BG);
        teamLabel.setOpaque(true);
        teamLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        teamPanel.add(teamLabel, BorderLayout.NORTH);
        teamPanel.add(teamScroll, BorderLayout.CENTER);

        // TOURNAMENT TABLE
        tournamentModel = new DefaultTableModel(new String[]{"Tournament ID", "Name"}, 0);
        tournamentTable = new JTable(tournamentModel);
        styleTable(tournamentTable);
        JScrollPane tourScroll = new JScrollPane(tournamentTable);
        styleScroll(tourScroll);

        JPanel tourPanel = new JPanel(new BorderLayout());
        tourPanel.setBackground(PANEL_BG);
        tourPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HEADER_BG, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        JLabel tourLabel = new JLabel("  Select a Tournament", SwingConstants.LEFT);
        tourLabel.setFont(new Font("Arial", Font.BOLD, 13));
        tourLabel.setForeground(TEXT);
        tourLabel.setBackground(HEADER_BG);
        tourLabel.setOpaque(true);
        tourLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        tourPanel.add(tourLabel, BorderLayout.NORTH);
        tourPanel.add(tourScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(teamPanel);
        splitPane.setRightComponent(tourPanel);

        // ---------------- REGISTER TABLE ----------------
        registerModel = new DefaultTableModel(new String[]{"Team ID", "Team Name", "Tournament ID", "Tournament Name"}, 0);
        registerTable = new JTable(registerModel);
        styleTable(registerTable);
        JScrollPane registerScroll = new JScrollPane(registerTable);
        styleScroll(registerScroll);

        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.setBackground(PANEL_BG);
        JLabel registerLabel = new JLabel("  Current Registrations", SwingConstants.LEFT);
        registerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        registerLabel.setForeground(TEXT);
        registerLabel.setBackground(HEADER_BG);
        registerLabel.setOpaque(true);
        registerLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        registerPanel.add(registerLabel, BorderLayout.NORTH);
        registerPanel.add(registerScroll, BorderLayout.CENTER);
        registerPanel.setBorder(BorderFactory.createLineBorder(HEADER_BG, 1));

        // ---------------- CENTER SPLIT ----------------
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.5);
        centerSplit.setDividerSize(8);
        centerSplit.setTopComponent(splitPane);
        centerSplit.setBottomComponent(registerPanel);

        // ---------------- BUTTON BAR ----------------
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonBar.setBackground(HEADER_BG);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setBackground(new Color(60, 100, 160));
        styleButton(refreshBtn);

        JButton assignBtn = new JButton("Assign Team to Tournament");
        assignBtn.setBackground(new Color(60, 140, 100));
        styleButton(assignBtn);

        JButton deleteBtn = new JButton("Remove Registration");
        deleteBtn.setBackground(new Color(180, 70, 70));
        styleButton(deleteBtn);

        JButton clearBtn = new JButton("Clear Selection");
        clearBtn.setBackground(new Color(100, 100, 120));
        styleButton(clearBtn);

        buttonBar.add(refreshBtn);
        buttonBar.add(assignBtn);
        buttonBar.add(deleteBtn);
        buttonBar.add(clearBtn);

        // ---------------- ACTIONS ----------------
        assignBtn.addActionListener(e -> assignTeam());
        clearBtn.addActionListener(e -> clearSelection());
        refreshBtn.addActionListener(e -> refreshData());
        deleteBtn.addActionListener(e -> deleteRegistration());

        // ---------------- LAYOUT ----------------
        add(centerSplit, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);

        loadTeams();
        loadTournaments();
        loadRegisters();
    }

    private void deleteRegistration() {
        int row = registerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a registration to remove!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int teamId = (int) registerModel.getValueAt(row, 0);
        int tournamentId = (int) registerModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove this team from this tournament?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                "DELETE FROM registers WHERE team_id = ? AND tournament_id = ?")) {
            ps.setInt(1, teamId);
            ps.setInt(2, tournamentId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Registration removed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No matching registration found.");
            }
            loadRegisters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        loadTeams();
        loadTournaments();
        loadRegisters();
        clearSelection();
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearSelection() {
        teamTable.clearSelection();
        tournamentTable.clearSelection();
        registerTable.clearSelection();
    }

    private void loadTeams() {
        teamModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT team_id, team_name FROM team ORDER BY team_id")) {
            while (rs.next()) {
                teamModel.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Team load error: " + e.getMessage());
        }
    }

    private void loadTournaments() {
        tournamentModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT tournament_id, name FROM tournament ORDER BY tournament_id")) {
            while (rs.next()) {
                tournamentModel.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tournament load error: " + e.getMessage());
        }
    }

    private void loadRegisters() {
        registerModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT r.team_id, t.team_name, r.tournament_id, to2.name " +
                "FROM registers r " +
                "JOIN team t ON r.team_id = t.team_id " +
                "JOIN tournament to2 ON r.tournament_id = to2.tournament_id " +
                "ORDER BY r.tournament_id")) {
            while (rs.next()) {
                registerModel.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2),
                    rs.getInt(3), rs.getString(4)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Register load error: " + e.getMessage());
        }
    }

    private void assignTeam() {
        int teamRow = teamTable.getSelectedRow();
        int tourRow = tournamentTable.getSelectedRow();
        if (teamRow == -1 || tourRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a team and a tournament!");
            return;
        }
        int teamId = (int) teamModel.getValueAt(teamRow, 0);
        int tournamentId = (int) tournamentModel.getValueAt(tourRow, 0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO registers VALUES (?, ?)")) {
            ps.setInt(1, teamId);
            ps.setInt(2, tournamentId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Team assigned successfully!");
            loadRegisters();
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "This team is already registered in this tournament!", "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Assign error: " + e.getMessage());
        }
    }

    private void styleButton(JButton btn) {
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
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
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Arial", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 35));
        h.setReorderingAllowed(false);
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(HEADER_BG, 1));
        sp.getViewport().setBackground(new Color(155, 180, 210));
    }
}