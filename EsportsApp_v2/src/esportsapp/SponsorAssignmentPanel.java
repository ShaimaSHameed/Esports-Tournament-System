package esportsapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class SponsorAssignmentPanel extends JPanel {

    private JTable sponsorTable, teamTable, tournamentTable;
    private DefaultTableModel sponsorModel, teamModel, tournamentModel;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);

    private static final Color TABLE_BG = new Color(155, 180, 210);

    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color TEXT = Color.WHITE;

    public SponsorAssignmentPanel() {

        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setOpaque(true);

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        topPanel.setBackground(PANEL_BG);
        topPanel.setOpaque(true);

        sponsorModel = new DefaultTableModel(new String[]{"Sponsor ID", "Name"}, 0);
        sponsorTable = new JTable(sponsorModel);
        styleTable(sponsorTable);

        teamModel = new DefaultTableModel(new String[]{"Team ID", "Team Name"}, 0);
        teamTable = new JTable(teamModel);
        styleTable(teamTable);

        tournamentModel = new DefaultTableModel(new String[]{"Tournament ID", "Name"}, 0);
        tournamentTable = new JTable(tournamentModel);
        styleTable(tournamentTable);

        JScrollPane sp1 = new JScrollPane(sponsorTable);
        JScrollPane sp2 = new JScrollPane(teamTable);
        JScrollPane sp3 = new JScrollPane(tournamentTable);

        styleScroll(sp1);
        styleScroll(sp2);
        styleScroll(sp3);

        topPanel.add(sp1);
        topPanel.add(sp2);
        topPanel.add(sp3);

        JButton loadBtn = new JButton("Load Data");
        styleButton(loadBtn);
        JButton assignBtn = new JButton("Assign Sponsor");
        JButton clearBtn = new JButton("Clear Selection");

        styleButton(assignBtn);
        styleButton(clearBtn);

        assignBtn.addActionListener(e -> assignSponsor());
        clearBtn.addActionListener(e -> clearSelection());
        loadBtn.addActionListener(e -> loadData());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonBar.setBackground(PANEL_BG);
        buttonBar.setOpaque(true);

        buttonBar.add(assignBtn);
        buttonBar.add(clearBtn);
        buttonBar.add(loadBtn);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(BG);
        centerWrapper.setOpaque(true);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(PANEL_BG);
        inner.setOpaque(true);
        inner.add(topPanel, BorderLayout.CENTER);

        centerWrapper.add(inner, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);

        teamTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                filterTournamentsByTeam();
            }
        });

        loadData();
    }

    private void filterTournamentsByTeam() {

        int row = teamTable.getSelectedRow();

        // If nothing selected → show all tournaments again
        if (row == -1) {
            loadTournamentsOnly();
            return;
        }

        int teamId = (int) teamModel.getValueAt(row, 0);

        tournamentModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT t.tournament_id, t.name "
                + "FROM tournament t JOIN registers r "
                + "ON t.tournament_id = r.tournament_id "
                + "WHERE r.team_id = ?"
        )) {

            ps.setInt(1, teamId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tournamentModel.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage());
        }
    }

    private void loadTournamentsOnly() {
        tournamentModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT tournament_id, name FROM tournament")) {

            while (rs.next()) {
                tournamentModel.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tournament load error: " + e.getMessage());
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(70, 100, 140));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(new Color(70, 100, 140), 1));
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBackground(TABLE_BG);
    }

    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG);
        t.setForeground(TEXT);
        t.setSelectionBackground(new Color(70, 160, 180));
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

    private void clearSelection() {
        sponsorTable.clearSelection();
        teamTable.clearSelection();
        tournamentTable.clearSelection();
    }

    private void loadData() {

        sponsorModel.setRowCount(0);
        teamModel.setRowCount(0);
        tournamentModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            Statement st = con.createStatement();
            ResultSet rs;

            rs = st.executeQuery("SELECT sponsor_id, name FROM sponsor");
            while (rs.next()) {
                sponsorModel.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }

            rs = st.executeQuery("SELECT team_id, team_name FROM team");
            while (rs.next()) {
                teamModel.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }

            rs = st.executeQuery("SELECT tournament_id, name FROM tournament");
            while (rs.next()) {
                tournamentModel.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load error: " + e.getMessage());
        }
    }

    private void assignSponsor() {
        int s = sponsorTable.getSelectedRow();
        int t = teamTable.getSelectedRow();
        int tr = tournamentTable.getSelectedRow();

        if (s == -1 || t == -1 || tr == -1) {
            JOptionPane.showMessageDialog(this, "Select sponsor, team, and tournament!");
            return;
        }

        int sponsorId = (int) sponsorModel.getValueAt(s, 0);
        int teamId = (int) teamModel.getValueAt(t, 0);
        int tournamentId = (int) tournamentModel.getValueAt(tr, 0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement checkTeam = con.prepareStatement(
                    "SELECT COUNT(*) FROM registers WHERE team_id = ? AND tournament_id = ?"
            );
            checkTeam.setInt(1, teamId);
            checkTeam.setInt(2, tournamentId);

            ResultSet rs1 = checkTeam.executeQuery();
            rs1.next();

            if (rs1.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this,
                        "This team is NOT registered in the selected tournament!");
                return;
            }

            PreparedStatement checkSponsor = con.prepareStatement(
                    "SELECT COUNT(*) FROM sponsorship WHERE sponsor_id = ? AND tournament_id = ?"
            );
            checkSponsor.setInt(1, sponsorId);
            checkSponsor.setInt(2, tournamentId);

            ResultSet rs2 = checkSponsor.executeQuery();
            rs2.next();

            if (rs2.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "This sponsor already sponsors a team in this tournament!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sponsorship VALUES (?, ?, ?)"
            );

            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            ps.setInt(3, sponsorId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Sponsor assigned successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Assign error: " + e.getMessage());
        }
    }
}
