import javax.swing.*;
import java.awt.*;

/** First-run form for creating the reserved administrator account. */
public final class AdminSetupPage {
    private final JPanel contentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmField = new JPasswordField(20);
    private SetupAction setupAction;

    public AdminSetupPage() {
        JPanel form = AuthPageStyle.createCard();
        AuthPageStyle.styleField(nameField);
        AuthPageStyle.styleField(addressField);
        AuthPageStyle.styleField(emailField);
        AuthPageStyle.styleField(phoneField);
        AuthPageStyle.styleField(passwordField);
        AuthPageStyle.styleField(confirmField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 12, 8);
        form.add(AuthPageStyle.createTitle("Set up your administrator",
                "This one-time step creates the first account"), gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 8, 5, 8);
        addField(form, gbc, 1, "Name:", nameField);
        addField(form, gbc, 2, "Address:", addressField);
        addField(form, gbc, 3, "Email:", emailField);
        addField(form, gbc, 4, "Phone number:", phoneField);
        addField(form, gbc, 5, "Admin username:", new JLabel("muddy (reserved)"));
        addField(form, gbc, 6, "Password:", passwordField);
        addField(form, gbc, 7, "Confirm password:", confirmField);

        JButton createButton = new JButton("Create Administrator");
        AuthPageStyle.stylePrimaryButton(createButton);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 8, 4, 8);
        form.add(createButton, gbc);
        createButton.addActionListener(event -> {
            if (setupAction != null) {
                setupAction.create(nameField.getText(), addressField.getText(), emailField.getText(),
                        phoneField.getText(), passwordField.getPassword(), confirmField.getPassword());
            }
        });

        contentPanel = AuthPageStyle.createPage("Welcome to<br>PhoneBook.",
                "Create the administrator account to secure and manage this phonebook.", form);
    }

    private static void addField(JPanel panel, GridBagConstraints gbc, int row,
                                 String labelText, Component field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setSetupAction(SetupAction action) {
        setupAction = action;
    }

    public void clearPasswords() {
        passwordField.setText("");
        confirmField.setText("");
    }

    public interface SetupAction {
        void create(String name, String address, String email, String phoneNumber,
                    char[] password, char[] confirmation);
    }
}
