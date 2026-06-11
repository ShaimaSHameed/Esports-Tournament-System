package esportsapp;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("The Lobby - Esports Management System");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(100, 130, 170));

        JLabel title = new JLabel("THE LOBBY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));

        JLabel sub = new JLabel("ESPORTS MANAGEMENT SYSTEM", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.BOLD, 13));
        sub.setForeground(Color.WHITE);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setBackground(new Color(100, 130, 170));
        top.add(title);
        top.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(140, 165, 200));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel uLbl = new JLabel("Username:");
        uLbl.setForeground(Color.WHITE);
        uLbl.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel pLbl = new JLabel("Password:");
        pLbl.setForeground(Color.WHITE);
        pLbl.setFont(new Font("Arial", Font.BOLD, 12));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);

        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;

        form.add(uLbl, g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        userField = new JTextField(15);
        form.add(userField, g);

        g.gridx = 0;
        g.gridy = 1;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;

        form.add(pLbl, g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        passField = new JPasswordField(15);
        form.add(passField, g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;

        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;
        g.anchor = GridBagConstraints.CENTER;
        JButton login = new JButton("LOGIN");

        login.setBackground(new Color(70, 160, 180));
        login.setForeground(Color.WHITE);
        login.setFont(new Font("Arial", Font.BOLD, 14));

        login.setFocusPainted(false);
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        login.setOpaque(true);
        login.setBorderPainted(false);
        login.setContentAreaFilled(true);

        login.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        form.add(login, g);

        login.addActionListener(e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM loggedusers WHERE username=? AND password=?")) {
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    dispose();
                    new MainFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        main.add(top, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        add(main);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
