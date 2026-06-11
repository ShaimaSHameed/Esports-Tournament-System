package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class GamesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField, genreField;
    private int selectedId = -1;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;
    private boolean updateMode = false;

    public GamesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Game ID", "Game Name", "Genre"};
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

        idField = addField(form, g, "Game ID:", 0, 0);
        nameField = addField(form, g, "Game Name:", 0, 2);
        genreField = addField(form, g, "Genre:", 0, 4);

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
        g.gridy = 1;
        g.gridwidth = 6;
        g.weightx = 0;
        form.add(btnPanel, g);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());

                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                genreField.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");

                setUpdateMode(true);
            }
        });

        load.addActionListener(e -> loadGames());
        insert.addActionListener(e -> insertGame());
        update.addActionListener(e -> updateGame());
        delete.addActionListener(e -> deleteGame());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadGames();
    }

    private void setUpdateMode(boolean mode) {
        updateMode = mode;
        idField.setEnabled(!mode);
    }

    private boolean validateGame(boolean isUpdate) {

        if (idField.getText().trim().isEmpty()
                || nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Game ID and Name are required!");
            return false;
        }

        int id;

        try {
            id = Integer.parseInt(idField.getText().trim());

            if (id <= 0 || id > 9999) {
                JOptionPane.showMessageDialog(this, "Game ID must be 1–9999");
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Game ID must be numeric");
            return false;
        }

        // duplicate check only on insert
        if (!isUpdate && gameIdExists(id)) {
            JOptionPane.showMessageDialog(this, "Game ID already exists!");
            return false;
        }

        if (!nameField.getText().matches("[a-zA-Z0-9\\s]+")) {
            JOptionPane.showMessageDialog(this, "Game name invalid");
            return false;
        }

        if (genreField.getText().length() > 25) {
            JOptionPane.showMessageDialog(this, "Genre max 25 characters");
            return false;
        }

        return true;
    }

    private boolean gameIdExists(int id) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM game WHERE game_id = ?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
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

    private void loadGames() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT game_id, game_name, genre FROM game ORDER BY game_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertGame() {

        if (!validateGame(false)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO game VALUES (?,?,?)")) {

            ps.setInt(1, Integer.parseInt(idField.getText().trim()));
            ps.setString(2, nameField.getText().trim());
            ps.setString(3, genreField.getText().trim());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Game inserted successfully!");
            loadGames();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateGame() {

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a game first!");
            return;
        }

        if (!validateGame(true)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "UPDATE game SET game_name=?, genre=? WHERE game_id=?")) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, genreField.getText().trim());
            ps.setInt(3, selectedId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Game updated successfully!");
            loadGames();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteGame() {

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a game first!");
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM game WHERE game_id=?")) {

            ps.setInt(1, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Game deleted successfully!");
            loadGames();
            clearFields();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete game: it is used in matches!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        selectedId = -1;

        idField.setText("");
        nameField.setText("");
        genreField.setText("");

        table.clearSelection();

        setUpdateMode(false);
    }
}
