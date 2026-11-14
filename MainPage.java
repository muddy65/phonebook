import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Signed-in user's profile and private contacts. */
public class MainPage extends JPanel {
    private static final Color NAVY = new Color(18, 39, 67);
    private static final Color TEAL = new Color(22, 119, 120);
    private final DatabaseHelper.UserAccount user;
    private final DefaultTableModel tableModel;
    private final JLabel contactsCountLabel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JButton navigationButton;
    private final JTable contactsTable;
    private Runnable onLogout;
    private Runnable onAccountDeleted;

    public MainPage(DatabaseHelper.UserAccount user) throws SQLException {
        this.user = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(235, 242, 249));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(17, 22, 17, 18));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("PHONEBOOK");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Your personal contact space");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(194, 210, 229));
        heading.add(title);
        heading.add(Box.createVerticalStrut(3));
        heading.add(subtitle);
        header.add(heading, BorderLayout.WEST);
        navigationButton = new JButton("Log out");
        navigationButton.setBackground(new Color(33, 57, 87));
        navigationButton.setForeground(Color.WHITE);
        navigationButton.setFocusPainted(false);
        navigationButton.addActionListener(event -> {
            if (onLogout != null) onLogout.run();
        });
        header.add(navigationButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(createProfilePanel(), BorderLayout.NORTH);

        String[] columns = {"Contact ID", "Name", "Phone number", "Address", "Relationship", "Other number"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        contactsTable = new JTable(tableModel);
        contactsTable.setRowHeight(32);
        contactsTable.setFillsViewportHeight(true);
        contactsTable.setAutoCreateRowSorter(true);
        contactsTable.setShowVerticalLines(false);
        contactsTable.setGridColor(new Color(232, 237, 243));
        contactsTable.setSelectionBackground(new Color(219, 237, 239));
        contactsTable.setSelectionForeground(NAVY);
        contactsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contactsTable.getTableHeader().setBackground(NAVY);
        contactsTable.getTableHeader().setForeground(Color.WHITE);
        contactsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        contactsTable.getTableHeader().setReorderingAllowed(false);
        contactsTable.setDefaultRenderer(Object.class, new StripedCellRenderer());
        sorter = new TableRowSorter<>(tableModel);
        contactsTable.setRowSorter(sorter);
        contactsTable.removeColumn(contactsTable.getColumnModel().getColumn(0));
        JScrollPane tableScroll = new JScrollPane(contactsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(222, 230, 239)));

        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 230, 239)),
                BorderFactory.createEmptyBorder(16, 18, 18, 18)));
        JPanel tableHeading = new JPanel(new BorderLayout(12, 0));
        tableHeading.setOpaque(false);
        contactsCountLabel = new JLabel("Your contacts");
        contactsCountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        contactsCountLabel.setForeground(NAVY);
        tableHeading.add(contactsCountLabel, BorderLayout.WEST);
        JTextField searchField = new JTextField(18);
        searchField.setToolTipText("Search your contacts");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateFilter() {
                String query = searchField.getText().trim();
                sorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent event) { updateFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent event) { updateFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent event) { updateFilter(); }
        });
        tableHeading.add(searchField, BorderLayout.EAST);
        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);
        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JButton deleteAccountButton = new JButton("Delete account");
        styleDangerButton(deleteAccountButton);
        deleteAccountButton.addActionListener(event -> deleteAccount());
        footer.add(deleteAccountButton, BorderLayout.WEST);

        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerActions.setOpaque(false);
        JButton deleteContactButton = new JButton("Delete selected contact");
        styleDangerButton(deleteContactButton);
        deleteContactButton.setPreferredSize(new Dimension(210, 42));
        deleteContactButton.addActionListener(event -> deleteSelectedContact());
        JButton addContactButton = new JButton("Add contact");
        addContactButton.setBackground(TEAL);
        addContactButton.setForeground(Color.WHITE);
        addContactButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addContactButton.setFocusPainted(false);
        addContactButton.setPreferredSize(new Dimension(160, 42));
        addContactButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addContactButton.addActionListener(event -> showAddContactDialog());
        footerActions.add(deleteContactButton);
        footerActions.add(addContactButton);
        footer.add(footerActions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        refreshContacts();
    }

    private JPanel createProfilePanel() {
        JPanel profile = new JPanel(new GridLayout(1, 5, 10, 0));
        profile.setBackground(Color.WHITE);
        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 222, 235)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        profile.add(createProfileTile("NAME", user.name));
        profile.add(createProfileTile("USERNAME", user.username));
        profile.add(createProfileTile("ADDRESS", user.address));
        profile.add(createProfileTile("EMAIL", user.email));
        profile.add(createProfileTile("PHONE", user.phoneNumber));
        return profile;
    }

    private JPanel createProfileTile(String label, String value) {
        JPanel tile = new JPanel();
        tile.setOpaque(false);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("SansSerif", Font.BOLD, 10));
        labelText.setForeground(new Color(104, 120, 139));
        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueText.setForeground(NAVY);
        valueText.setToolTipText(value);
        tile.add(labelText);
        tile.add(Box.createVerticalStrut(7));
        tile.add(valueText);
        return tile;
    }

    private void refreshContacts() {
        try {
            List<DatabaseHelper.Contact> contacts = DatabaseHelper.getContacts(user.id);
            tableModel.setRowCount(0);
            for (DatabaseHelper.Contact contact : contacts) {
                tableModel.addRow(new Object[]{contact.id, upper(contact.name), upper(contact.phoneNumber),
                        upper(contact.address), upper(contact.relationship), upper(contact.otherNumber)});
            }
            contactsCountLabel.setText("Your contacts  ·  " + contacts.size());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteSelectedContact() {
        int selectedRow = contactsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a contact in the table first.",
                    "No contact selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = contactsTable.convertRowIndexToModel(selectedRow);
        long contactId = ((Number) tableModel.getValueAt(modelRow, 0)).longValue();
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this contact from your phonebook?", "Delete contact",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            if (DatabaseHelper.deleteContact(user.id, contactId)) {
                refreshContacts();
            } else {
                JOptionPane.showMessageDialog(this, "That contact was already removed.",
                        "Contact not found", JOptionPane.INFORMATION_MESSAGE);
                refreshContacts();
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteAccount() {
        int answer = JOptionPane.showConfirmDialog(this,
                "This permanently deletes your account and all of its contacts. Continue?",
                "Delete account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        JPasswordField passwordCheck = new JPasswordField(18);
        int verified = JOptionPane.showConfirmDialog(this,
                new Object[]{"Enter your current password to confirm:", passwordCheck},
                "Confirm account deletion", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (verified != JOptionPane.OK_OPTION) {
            passwordCheck.setText("");
            return;
        }

        char[] password = passwordCheck.getPassword();
        passwordCheck.setText("");
        try {
            Validation.LoginResult result = Validation.logIn(user.username, password);
            if (result.error != null) {
                JOptionPane.showMessageDialog(this, "The password was not correct. Account kept.",
                        "Deletion cancelled", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!DatabaseHelper.deleteUser(user.id)) {
                JOptionPane.showMessageDialog(this, "The account could not be found.",
                        "Account not deleted", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Your account and contacts have been deleted.",
                    "Account deleted", JOptionPane.INFORMATION_MESSAGE);
            if (onAccountDeleted != null) onAccountDeleted.run();
            else if (onLogout != null) onLogout.run();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void showAddContactDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Add contact", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField relationshipField = new JTextField(20);
        JTextField otherNumberField = new JTextField(20);
        addContactField(form, gbc, 0, "Name:", nameField);
        addContactField(form, gbc, 1, "Phone number:", phoneField);
        addContactField(form, gbc, 2, "Address:", addressField);
        addContactField(form, gbc, 3, "Relationship:", relationshipField);
        addContactField(form, gbc, 4, "Other number:", otherNumberField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());
        JButton saveButton = new JButton("Save contact");
        saveButton.addActionListener(event -> {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String relationship = relationshipField.getText();
            String other = otherNumberField.getText();
            String error = Validation.validateContact(name, phone, address, relationship, other);
            if (error != null) {
                JOptionPane.showMessageDialog(dialog, error, "Check contact details", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                DatabaseHelper.addContact(user.id, name.trim(), phone.trim(), address.trim(),
                        relationship.trim(), other.trim());
                refreshContacts();
                dialog.dispose();
            } catch (SQLException ex) {
                showDatabaseError(ex);
            }
        });
        buttons.add(cancelButton);
        buttons.add(saveButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(buttons, gbc);
        dialog.setContentPane(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addContactField(JPanel form, GridBagConstraints gbc, int row,
                                 String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        form.add(field, gbc);
    }

    private void showDatabaseError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "PhoneBook", JOptionPane.ERROR_MESSAGE);
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void setNavigation(String label, Runnable action) {
        navigationButton.setText(label);
        this.onLogout = action;
    }

    public void setOnAccountDeleted(Runnable onAccountDeleted) {
        this.onAccountDeleted = onAccountDeleted;
    }

    private static String upper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private void styleDangerButton(JButton button) {
        button.setBackground(new Color(180, 35, 24));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 42));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class StripedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component cell = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(246, 248, 251));
                cell.setForeground(new Color(43, 58, 76));
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
            return cell;
        }
    }
}
