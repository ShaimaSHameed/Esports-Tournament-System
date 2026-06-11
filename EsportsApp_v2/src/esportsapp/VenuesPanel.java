package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class VenuesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, cityField, countryField, nameField;
    private int selectedId = -1;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;

    public VenuesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Venue ID", "City", "Country", "Venue Name"};
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

        idField = addField(form, g, "Venue ID:", 0, 0);
        cityField = addField(form, g, "City:", 0, 2);
        countryField = addField(form, g, "Country:", 0, 4);
        nameField = addField(form, g, "Venue Name:", 1, 0);

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

                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());

                idField.setText(model.getValueAt(row, 0).toString());
                cityField.setText(model.getValueAt(row, 1).toString());
                countryField.setText(model.getValueAt(row, 2).toString());
                nameField.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");

                // 🔒 lock ID in update mode
                setUpdateMode(true);
            }
        });

        load.addActionListener(e -> loadVenues());
        insert.addActionListener(e -> insertVenue());
        update.addActionListener(e -> updateVenue());
        delete.addActionListener(e -> deleteVenue());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadVenues();
    }

    private boolean validateVenueInputs(boolean isUpdate) {

        // 1. Empty checks (except venue_name)
        if (idField.getText().trim().isEmpty()
                || cityField.getText().trim().isEmpty()
                || countryField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "City, Country, and ID are required!");
            return false;
        }

        int venueId;

        // 2. Venue ID validation
        try {
            venueId = Integer.parseInt(idField.getText().trim());

            if (venueId <= 0 || venueId > 99999) {
                JOptionPane.showMessageDialog(this, "Venue ID must be between 1 and 99999");
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Venue ID must be a number");
            return false;
        }

        // 3. Duplicate ID check (insert only)
        if (!isUpdate && venueIdExists(venueId)) {
            JOptionPane.showMessageDialog(this, "Venue ID already exists!");
            return false;
        }

        // 4. City validation (letters + spaces only)
        if (!cityField.getText().matches("[a-zA-Z\\s]+")) {
            JOptionPane.showMessageDialog(this, "City must contain only letters");
            return false;
        }

        // 5. Country validation
        if (!countryField.getText().matches("[a-zA-Z\\s]+")) {
            JOptionPane.showMessageDialog(this, "Country must contain only letters");
            return false;
        }

        // 6. Venue name length
        if (nameField.getText().length() > 50) {
            JOptionPane.showMessageDialog(this, "Venue name max length is 50");
            return false;
        }

        return true;
    }

    private boolean venueIdExists(int id) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM venue WHERE venue_id = ?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking venue ID: " + e.getMessage());
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

    private void loadVenues() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT venue_id, city, country, venue_name FROM venue ORDER BY venue_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertVenue() {

        if (!validateVenueInputs(false)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "INSERT INTO venue VALUES (?,?,?,?)")) {

            ps.setInt(1, Integer.parseInt(idField.getText().trim()));
            ps.setString(2, cityField.getText().trim());
            ps.setString(3, countryField.getText().trim());
            ps.setString(4, nameField.getText().trim());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Venue inserted successfully!");
            loadVenues();
            clearFields();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Venue ID already exists (PK violation)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateVenue() {

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue first!");
            return;
        }

        if (!validateVenueInputs(true)) {
            return;
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "UPDATE venue SET city=?, country=?, venue_name=? WHERE venue_id=?")) {

            ps.setString(1, cityField.getText().trim());
            ps.setString(2, countryField.getText().trim());
            ps.setString(3, nameField.getText().trim());
            ps.setInt(4, selectedId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Venue updated successfully!");
            loadVenues();
            clearFields();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void setUpdateMode(boolean updateMode) {
        idField.setEnabled(!updateMode); // disabled in update mode
    }

    private void deleteVenue() {
        if (venueInUse(selectedId)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete venue: it is used by a tournament!");
            return;
        }
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM venue WHERE venue_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Venue deleted successfully!");
            loadVenues();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        selectedId = -1;

        idField.setText("");
        cityField.setText("");
        countryField.setText("");
        nameField.setText("");

        table.clearSelection();

        //  back to insert mode
        setUpdateMode(false);
    }

    private boolean venueInUse(int venueId) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM tournament WHERE venue_id = ?")) {

            ps.setInt(1, venueId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return true; // safer fallback
        }
    }
}
