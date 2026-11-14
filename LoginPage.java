import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPage {
    private final JPanel contentPanel;
    private final JTextField userNameField;
    private final JPasswordField passwordField;
    private Runnable onSignupClick;
    private Runnable onForgotPasswordClick;
    private LoginAction loginAction;

    public LoginPage() {
        JPanel formPanel = AuthPageStyle.createCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = AuthPageStyle.createTitle("Welcome back", "Sign in to your contact space");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 10, 20, 10);
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel userNameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(userNameLabel, gbc);

        userNameField = new JTextField(22);
        AuthPageStyle.styleField(userNameField);
        userNameField.setToolTipText("Enter your username");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(userNameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(22);
        AuthPageStyle.styleField(passwordField);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        AuthPageStyle.stylePrimaryButton(loginButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(16, 10, 8, 10);
        formPanel.add(loginButton, gbc);
        loginButton.addActionListener(event -> {
            if (loginAction != null) {
                loginAction.logIn(userNameField.getText(), passwordField.getPassword());
            }
        });

        JLabel signupLink = new JLabel(
                "<html>Don't have an account? <a href='signup'>Sign up</a></html>");
        signupLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        signupLink.setForeground(new Color(75, 85, 95));
        signupLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupLink.setToolTipText("Go to the signup page");
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (onSignupClick != null) {
                    onSignupClick.run();
                }
            }
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 10, 0, 10);
        formPanel.add(signupLink, gbc);

        JLabel forgotPasswordLink = new JLabel(
                "<html><a href='reset'>Forgot your password?</a></html>");
        forgotPasswordLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotPasswordLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.setToolTipText("Reset your password");
        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (onForgotPasswordClick != null) onForgotPasswordClick.run();
            }
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 10, 0, 10);
        formPanel.add(forgotPasswordLink, gbc);

        contentPanel = AuthPageStyle.createPage(
                "Your people,<br>always close.",
                "Keep the names and numbers that matter in one calm, private place.", formPanel);

        
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setOnSignupClick(Runnable onSignupClick) {
        this.onSignupClick = onSignupClick;
    }

    public void setOnForgotPasswordClick(Runnable onForgotPasswordClick) {
        this.onForgotPasswordClick = onForgotPasswordClick;
    }

    public void setLoginAction(LoginAction loginAction) {
        this.loginAction = loginAction;
    }

    public void clearPassword() {
        passwordField.setText("");
    }

    public interface LoginAction {
        void logIn(String username, char[] password);
    }
}
