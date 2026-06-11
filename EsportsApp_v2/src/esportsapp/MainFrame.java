package esportsapp;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane createSponsorTabs() {

        JTabbedPane sponsorTabs = new JTabbedPane();
        sponsorTabs.setFont(new Font("Arial", Font.BOLD, 12));

        // Existing panel 
        sponsorTabs.addTab("Manage Sponsors", new SponsorsPanel());

        // New assignment panel
        sponsorTabs.addTab("Assign Sponsors", new SponsorAssignmentPanel());

        return sponsorTabs;
    }

    private JTabbedPane createTeamTabs() {

        JTabbedPane teamTabs = new JTabbedPane();
        teamTabs.setFont(new Font("Arial", Font.BOLD, 12));

        teamTabs.addTab("Manage Teams", new TeamsPanel());

        teamTabs.addTab("Assign to Matches", new TeamMatchAssignmentPanel());

        teamTabs.addTab("Assign a coach", new TeamCoachAssignPanel());
        return teamTabs;
    }

    private JTabbedPane createMatchTabs() {

        JTabbedPane matchTabs = new JTabbedPane();
        matchTabs.setFont(new Font("Arial", Font.BOLD, 12));

        matchTabs.addTab("Manage Matches", new MatchesPanel());
        matchTabs.addTab("Performance Stats", new PerformancePanel());

        matchTabs.addTab("Rounds", new RoundsPanel());

        return matchTabs;
    }

    public MainFrame() {
        setTitle("The Lobby - Esports Management System");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(70, 100, 140));
        header.setPreferredSize(new Dimension(950, 55));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("THE LOBBY  —  Esports Management System");
        title.setFont(new Font("Arial", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.setBackground(new Color(120, 150, 185));

        tabs.addTab("Players", new PlayersPanel());
        tabs.addTab("Registration", new RegistrationPanel());
        tabs.addTab("Teams", createTeamTabs());
        tabs.addTab("Tournaments", new TournamentsPanel());
        tabs.addTab("Matches", createMatchTabs());
        tabs.addTab("Venues", new VenuesPanel());
        tabs.addTab("Games", new GamesPanel());
        tabs.addTab("Contracts", new ContractPanel());
        tabs.addTab("Sponsors", createSponsorTabs());
        tabs.addTab("Queries", new QueriesPanel());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
