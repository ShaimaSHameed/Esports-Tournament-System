package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class QueriesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> queryBox;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color TEXT = Color.WHITE;

    private static final String[] QUERY_NAMES = {
        "1. Players with their Teams (Equi Join)",
        "2. Teams and Matches Played (Natural Join)",
        "3. Players and Stats - incl. no matches (Left Outer Join)",
        "4. Matches and Players - incl. no stats (Right Outer Join)",
        "5. Players sharing same Nationality (Self Join)",
        "6. Teams with above-average total Kills (Aggregate + Subquery)",
        "7. Tournaments with above-average Prize Pool (Aggregate + Subquery)",
        "8. Players who played at Dubai venue (Multi-row Subquery)",
        "9. Teams sharing a Sponsor with Team Liquid (Multi-row Subquery)",
        "10. Players with Kills above average of winning teams (3-level Nesting)",
        "11. Tournament Winners (Max Round Wins)",
        "12. Teams not playing in any Match",
        "13. Players not assigned to any Team"
    };

    private static final String[] QUERIES = {
        "SELECT p.first_name, p.last_name, p.nationality, p.role, t.team_name FROM player p, contract c, team t WHERE p.player_id = c.player_id AND c.team_id = t.team_id ORDER BY t.team_name",
        "SELECT team_name, match_id, match_date, map_name FROM team NATURAL JOIN plays_in NATURAL JOIN match ORDER BY match_date",
        "SELECT p.first_name, p.last_name, p.role, pf.match_id, pf.kills, pf.deaths, pf.assists FROM player p LEFT OUTER JOIN performs pf ON p.player_id = pf.player_id ORDER BY p.player_id",
        "SELECT p.first_name, p.last_name, pf.match_id, pf.kills, pf.deaths, pf.assists FROM performs pf RIGHT OUTER JOIN player p ON pf.player_id = p.player_id ORDER BY pf.match_id",
        "SELECT p1.first_name || ' ' || p1.last_name AS player_1, p2.first_name || ' ' || p2.last_name AS player_2, p1.nationality FROM player p1, player p2 WHERE p1.nationality = p2.nationality AND p1.player_id < p2.player_id ORDER BY p1.nationality",
        "SELECT t.team_name, SUM(pf.kills) AS total_kills FROM team t JOIN contract c ON t.team_id = c.team_id JOIN performs pf ON c.player_id = pf.player_id GROUP BY t.team_name HAVING SUM(pf.kills) > (SELECT AVG(team_kills) FROM (SELECT SUM(pf2.kills) AS team_kills FROM contract c2 JOIN performs pf2 ON c2.player_id = pf2.player_id GROUP BY c2.team_id)) ORDER BY total_kills DESC",
        "SELECT name, prize_pool, start_date, end_date FROM tournament WHERE prize_pool > (SELECT AVG(prize_pool) FROM tournament) ORDER BY prize_pool DESC",
        "SELECT first_name, last_name, nationality, role FROM player WHERE player_id IN (SELECT player_id FROM performs WHERE match_id IN (SELECT match_id FROM match WHERE tournament_id IN (SELECT tournament_id FROM tournament WHERE venue_id IN (SELECT venue_id FROM venue WHERE city = 'Dubai'))))",
        "SELECT DISTINCT t.team_name FROM team t WHERE t.team_id IN (SELECT team_id FROM sponsorship WHERE sponsor_id IN (SELECT sponsor_id FROM sponsorship WHERE team_id IN (SELECT team_id FROM team WHERE team_name = 'Team Liquid'))) AND t.team_name != 'Team Liquid'",
        "SELECT first_name, last_name, nationality, role FROM player WHERE player_id IN (SELECT player_id FROM performs WHERE kills > (SELECT AVG(kills) FROM performs WHERE player_id IN (SELECT player_id FROM contract WHERE team_id IN (SELECT DISTINCT winner FROM round WHERE winner IS NOT NULL)))) ORDER BY last_name",
        "SELECT t.tournament_id, t.name AS tournament_name, tm.team_name, COUNT(*) AS rounds_won\n"
        + "FROM round r\n"
        + "JOIN match m ON r.match_id = m.match_id\n"
        + "JOIN tournament t ON m.tournament_id = t.tournament_id\n"
        + "JOIN team tm ON r.winner = tm.team_id\n"
        + "GROUP BY t.tournament_id, t.name, tm.team_name\n"
        + "HAVING COUNT(*) = (\n"
        + "    SELECT MAX(cnt)\n"
        + "    FROM (\n"
        + "        SELECT COUNT(*) cnt\n"
        + "        FROM round r2\n"
        + "        JOIN match m2 ON r2.match_id = m2.match_id\n"
        + "        WHERE m2.tournament_id = t.tournament_id\n"
        + "        GROUP BY r2.winner\n"
        + "    ) x\n"
        + ")",
        "SELECT team_name FROM team WHERE team_id NOT IN (SELECT team_id FROM plays_in) ORDER BY team_name",
        "SELECT first_name, last_name, nationality, role FROM player WHERE player_id NOT IN (SELECT player_id FROM contract) ORDER BY first_name"
        
           
    };

    public QueriesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(PANEL_BG);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_BG, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel lbl = new JLabel("Select Query:  ");
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));

        queryBox = new JComboBox<>(QUERY_NAMES);
        queryBox.setBackground(new Color(170, 195, 220));
        queryBox.setForeground(new Color(40, 40, 40));
        queryBox.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton run = makeButton("Run Query", BTN_BLUE);

        topPanel.add(lbl, BorderLayout.WEST);
        topPanel.add(queryBox, BorderLayout.CENTER);
        topPanel.add(run, BorderLayout.EAST);

        model = new DefaultTableModel();
        table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(155, 180, 210));
        scroll.setBorder(BorderFactory.createLineBorder(HEADER_BG, 1));

        JLabel status = new JLabel("  Select a query and click Run", SwingConstants.LEFT);
        status.setForeground(new Color(220, 235, 255));
        status.setFont(new Font("Arial", Font.ITALIC, 12));
        status.setBackground(PANEL_BG);
        status.setOpaque(true);
        status.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        run.addActionListener(e -> {
            int idx = queryBox.getSelectedIndex();
            runQuery(QUERIES[idx], status, idx);
        });

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    private void runQuery(String sql, JLabel status, int queryIndex) {
        model.setRowCount(0);
        model.setColumnCount(0);

        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            // add original columns
            for (int i = 1; i <= cols; i++) {
                model.addColumn(meta.getColumnName(i));
            }

            // ADD KDA COLUMN ONLY FOR QUERY 3
            boolean addKDA = (queryIndex == 2);
            if (addKDA) {
                model.addColumn("KDA");
            }

            int rows = 0;

            while (rs.next()) {

                Object[] row = new Object[cols + (addKDA ? 1 : 0)];

                int deaths = 0;
                int kills = 0;
                int assists = 0;

                for (int i = 1; i <= cols; i++) {
                    Object val = rs.getObject(i);
                    row[i - 1] = val;

                    // Query 3 columns order:
                    // first_name, last_name, role, match_id, kills, deaths, assists
                    if (addKDA) {
                        if (i == 5) {
                            kills = (val == null) ? 0 : ((Number) val).intValue();
                        }
                        if (i == 6) {
                            deaths = (val == null) ? 0 : ((Number) val).intValue();
                        }
                        if (i == 7) {
                            assists = (val == null) ? 0 : ((Number) val).intValue();
                        }
                    }
                }

                if (addKDA) {
                    double kda = (kills + assists) / (double) Math.max(1, deaths);
                    row[cols] = String.format("%.2f", kda);
                }

                model.addRow(row);
                rows++;
            }

            status.setText("  Query returned " + rows + " row(s)");
            status.setForeground(Color.WHITE);

        } catch (Exception ex) {
            status.setText("  Error: " + ex.getMessage());
            status.setForeground(new Color(255, 150, 150));
        }
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
}
