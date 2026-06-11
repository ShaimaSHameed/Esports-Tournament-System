package esportsapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class SponsorsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField idField, nameField;

    private static final Color BG = new Color(100, 130, 170);
    private static final Color PANEL_BG = new Color(140, 165, 200);
    private static final Color HEADER_BG = new Color(70, 100, 140);
    private static final Color ACCENT = new Color(70, 160, 180);
    private static final Color BTN_RED = new Color(180, 70, 70);
    private static final Color BTN_BLUE = new Color(60, 100, 160);
    private static final Color BTN_GREEN = new Color(60, 140, 100);
    private static final Color TEXT = Color.WHITE;

    public SponsorsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Sponsor ID", "Name"};
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

        idField = addField(form, g, "Sponsor ID:", 0, 0);
        nameField = addField(form, g, "Name:", 0, 2);

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
        g.gridwidth = 4;
        g.weightx = 0;
        form.add(btnPanel, g);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());

                setEditMode(); // 🔒 lock ID when editing
            }
        });

        load.addActionListener(e -> loadSponsors());
        insert.addActionListener(e -> insertSponsor());
        update.addActionListener(e -> updateSponsor());
        delete.addActionListener(e -> deleteSponsor());
        clear.addActionListener(e -> clearFields());

        add(scroll, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadSponsors();
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

    private void loadSponsors() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT sponsor_id, name FROM sponsor ORDER BY sponsor_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertSponsor() {

        String idText = idField.getText().trim();
        String name = nameField.getText().trim();

        // validation
        if (idText.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        try {
            int id = Integer.parseInt(idText);

            try (Connection con = DBConnection.getConnection()) {

                PreparedStatement check = con.prepareStatement(
                        "SELECT COUNT(*) FROM sponsor WHERE sponsor_id=?"
                );
                check.setInt(1, id);

                ResultSet rs = check.executeQuery();
                rs.next();

                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Sponsor ID already exists!");
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO sponsor VALUES (?,?)"
                );

                ps.setInt(1, id);
                ps.setString(2, name);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Inserted successfully!");

                loadSponsors();
                clearFields();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID must be a valid number!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateSponsor() {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE sponsor SET name=? WHERE sponsor_id=?")) {
            ps.setString(1, nameField.getText());
            ps.setInt(2, Integer.parseInt(idField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sponsor updated successfully!");
            loadSponsors();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteSponsor() {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM sponsor WHERE sponsor_id=?")) {
            ps.setInt(1, Integer.parseInt(idField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sponsor deleted successfully!");
            loadSponsors();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        table.clearSelection();

        setInsertMode();
    }

    private void setInsertMode() {
        idField.setEditable(true);
    }

    private void setEditMode() {
        idField.setEditable(false);
    }
}
