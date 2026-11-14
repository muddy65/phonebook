import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignupPage {
    private final JPanel contentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneNumberField = new JTextField(20);
    private Runnable onLoginClick;
    private SignupAction signupAction;

    public SignupPage() {
        JPanel formPanel = AuthPageStyle.createCard();
        AuthPageStyle.styleField(nameField);
        AuthPageStyle.styleField(addressField);
        AuthPageStyle.styleField(usernameField);
        AuthPageStyle.styleField(passwordField);
        AuthPageStyle.styleField(confirmPasswordField);
        AuthPageStyle.styleField(emailField);
        AuthPageStyle.styleField(phoneNumberField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = AuthPageStyle.createTitle("Create your account", "Start your private phonebook");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 14, 8);
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 0;

        addField(formPanel, gbc, 1, "Name:", nameField);
        addField(formPanel, gbc, 2, "Address:", addressField);
        addField(formPanel, gbc, 3, "Username:", usernameField);
        addField(formPanel, gbc, 4, "Password:", passwordField);
        addField(formPanel, gbc, 5, "Confirm password:", confirmPasswordField);
        addField(formPanel, gbc, 6, "Email:", emailField);
        addField(formPanel, gbc, 7, "Phone number:", phoneNumberField);

        JButton createAccountButton = new JButton("Create Account");
        AuthPageStyle.stylePrimaryButton(createAccountButton);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 8, 8, 8);
        formPanel.add(createAccountButton, gbc);
        createAccountButton.addActionListener(event -> {
            if (signupAction != null) {
                signupAction.signUp(nameField.getText(), addressField.getText(),
                        usernameField.getText(), passwordField.getPassword(),
                        confirmPasswordField.getPassword(), emailField.getText(),
                        phoneNumberField.getText());
            }
        });

        JLabel loginLink = new JLabel(
                "<html>Already have an account? <a href='login'>Login</a></html>");
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginLink.setForeground(new Color(75, 85, 95));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.setToolTipText("Go to the login page");
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (onLoginClick != null) {
                    onLoginClick.run();
                }
            }
        });
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 8, 0, 8);
        formPanel.add(loginLink, gbc);

        contentPanel = AuthPageStyle.createPage(
                "A better way<br>to keep in touch.",
                "Save each person's details in a beautifully organized, personal directory.", formPanel);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row,
                          String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setOnLoginClick(Runnable onLoginClick) {
        this.onLoginClick = onLoginClick;
    }

    public void setSignupAction(SignupAction signupAction) {
        this.signupAction = signupAction;
    }

    public void clearPasswords() {
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    public interface SignupAction {
        void signUp(String name, String address, String username, char[] password,
                    char[] confirmation, String email, String phoneNumber);
    }
}
