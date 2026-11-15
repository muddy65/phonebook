import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/** Administrator overview with an inline user directory. */
public class AdminPage extends JPanel {
    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color TEAL = new Color(22, 119, 120);
    private final DefaultTableModel usersModel;
    private final JLabel usersCountLabel;
    private Runnable onLogout;
    private Runnable onShowPhonebook;

    public AdminPage() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(241, 245, 249));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(17, 22, 17, 18));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("PHONEBOOK  /  ADMIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Account management and directory overview");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(194, 210, 229));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        header.add(heading, BorderLayout.WEST);

        JButton logoutButton = new JButton("Log out");
        styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(event -> {
            if (onLogout != null) onLogout.run();
        });
        header.add(logoutButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel directory = new JPanel(new BorderLayout(0, 12));
        directory.setBackground(Color.WHITE);
        directory.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 230, 239)),
                BorderFactory.createEmptyBorder(17, 18, 18, 18)));

        JPanel directoryHeader = new JPanel(new BorderLayout(12, 0));
        directoryHeader.setOpaque(false);
        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel directoryTitle = new JLabel("Registered users");
        directoryTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
        directoryTitle.setForeground(NAVY);
        usersCountLabel = new JLabel("Loading user directory...");
        usersCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        usersCountLabel.setForeground(new Color(91, 106, 124));
        headerText.add(directoryTitle);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(usersCountLabel);
        directoryHeader.add(headerText, BorderLayout.WEST);

        JButton myPhonebookButton = new JButton("My phonebook");
        stylePrimaryButton(myPhonebookButton);
        myPhonebookButton.addActionListener(event -> {
            if (onShowPhonebook != null) onShowPhonebook.run();
        });
        directoryHeader.add(myPhonebookButton, BorderLayout.EAST);
        directory.add(directoryHeader, BorderLayout.NORTH);

        String[] columns = {"Name", "Username", "Email", "Phone number", "Address",
                "Password status", "Created"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable usersTable = new JTable(usersModel);
        styleTable(usersTable);
        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 228, 238)));
        directory.add(scroll, BorderLayout.CENTER);

        JLabel privacyNote = new JLabel("Passwords are stored as salted hashes and are never displayed.");
        privacyNote.setForeground(new Color(91, 106, 124));
        privacyNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        directory.add(privacyNote, BorderLayout.SOUTH);
        add(directory, BorderLayout.CENTER);

        refreshUsers();
    }

    private void refreshUsers() {
        BackgroundTask.run(this, DatabaseHelper::getAllUsers, users -> {
            usersModel.setRowCount(0);
            for (DatabaseHelper.UserSummary user : users) {
                usersModel.addRow(new Object[]{upper(user.name), upper(user.username), user.email,
                        upper(user.phoneNumber), upper(user.address), "PROTECTED", upper(user.createdAt)});
            }
            usersCountLabel.setText(users.size() + (users.size() == 1
                    ? " account in your phonebook" : " accounts in your phonebook"));
        }, ex -> {
            usersCountLabel.setText("Could not load accounts from the database.");
            JOptionPane.showMessageDialog(this, "Could not load users: " + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(232, 237, 243));
        table.setSelectionBackground(new Color(219, 237, 239));
        table.setSelectionForeground(NAVY);
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 42));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(33, 57, 87));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void setOnShowPhonebook(Runnable onShowPhonebook) {
        this.onShowPhonebook = onShowPhonebook;
    }

    private String upper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}
