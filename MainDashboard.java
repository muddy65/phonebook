import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private final LoginPage loginPage;
    private final SignupPage signupPage;
    private final PasswordResetPage passwordResetPage;
    private final AdminSetupPage adminSetupPage;
    private Component activeWorkspace;

    public MainDashboard() {
        super("PhoneBook");
        Image phonebookIcon = AuthPageStyle.loadPhonebookImage();
        if (phonebookIcon != null) setIconImage(phonebookIcon);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        loginPage = new LoginPage();
        signupPage = new SignupPage();
        passwordResetPage = new PasswordResetPage();
        adminSetupPage = new AdminSetupPage();

        loginPage.setOnSignupClick(() -> cardLayout.show(mainPanel, "SIGNUP"));
        loginPage.setOnForgotPasswordClick(() -> cardLayout.show(mainPanel, "RESET"));
        signupPage.setOnLoginClick(() -> cardLayout.show(mainPanel, "LOGIN"));
        loginPage.setLoginAction(this::logIn);
        signupPage.setSignupAction(this::signUp);
        passwordResetPage.setOnBackToLogin(() -> cardLayout.show(mainPanel, "LOGIN"));
        passwordResetPage.setResetAction(this::resetPassword);
        adminSetupPage.setSetupAction(this::createInitialAdmin);

        mainPanel.add(loginPage.getContentPanel(), "LOGIN");
        mainPanel.add(signupPage.getContentPanel(), "SIGNUP");
        mainPanel.add(passwordResetPage.getContentPanel(), "RESET");
        mainPanel.add(adminSetupPage.getContentPanel(), "ADMIN_SETUP");
        mainPanel.add(createStartupPanel(), "STARTUP");
        add(mainPanel);
        cardLayout.show(mainPanel, "STARTUP");
    }

    private JPanel createStartupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));
        JLabel status = new JLabel("Connecting securely to PhoneBook…");
        status.setFont(new Font("SansSerif", Font.BOLD, 18));
        status.setForeground(AuthPageStyle.NAVY);
        panel.add(status);
        return panel;
    }

    private void initializeApplication() {
        BackgroundTask.run(this, () -> {
            DatabaseHelper.initialize();
            return DatabaseHelper.isUserTableEmpty();
        }, isFirstRun -> cardLayout.show(mainPanel, isFirstRun ? "ADMIN_SETUP" : "LOGIN"), ex -> {
            showDatabaseError(ex);
            dispose();
        });
    }

    private void createInitialAdmin(String name, String address, String email, String phoneNumber,
                                    char[] password, char[] confirmation) {
        BackgroundTask.run(this, () -> Validation.createInitialAdmin(name, address, email, phoneNumber,
                password, confirmation), error -> {
            clear(password);
            clear(confirmation);
            adminSetupPage.clearPasswords();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Administrator setup",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Administrator account created. You can now log in as muddy.",
                    "Setup complete", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "LOGIN");
        }, ex -> {
            clear(password);
            clear(confirmation);
            adminSetupPage.clearPasswords();
            showDatabaseError(ex);
        });
    }

    private void signUp(String name, String address, String username, char[] password,
                        char[] confirmation, String email, String phoneNumber) {
        BackgroundTask.run(this, () -> Validation.signUp(name, address, username, password,
                confirmation, email, phoneNumber), error -> {
            clear(password);
            clear(confirmation);
            signupPage.clearPasswords();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Check signup details",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Account created. You can now log in.",
                    "Signup complete", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "LOGIN");
        }, ex -> {
            clear(password);
            clear(confirmation);
            signupPage.clearPasswords();
            showDatabaseError(ex);
        });
    }

    private void logIn(String username, char[] password) {
        BackgroundTask.run(this, () -> Validation.logIn(username, password), result -> {
            clear(password);
            loginPage.clearPassword();
            if (result.error != null) {
                JOptionPane.showMessageDialog(this, result.error, "Login failed",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (result.user.username.equalsIgnoreCase("muddy")) {
                AdminPage adminPage = new AdminPage();
                adminPage.setOnLogout(() -> cardLayout.show(mainPanel, "LOGIN"));
                adminPage.setOnShowPhonebook(() -> showAdminPhonebook(result.user, adminPage));
                showWorkspace(adminPage, "ADMIN");
            } else {
                MainPage dashboard = new MainPage(result.user);
                dashboard.setOnLogout(() -> cardLayout.show(mainPanel, "LOGIN"));
                dashboard.setOnAccountDeleted(() -> cardLayout.show(mainPanel, "LOGIN"));
                showWorkspace(dashboard, "DASHBOARD");
            }
        }, ex -> {
            clear(password);
            loginPage.clearPassword();
            showDatabaseError(ex);
        });
    }

    private void showAdminPhonebook(DatabaseHelper.UserAccount admin, AdminPage adminPage) {
        MainPage phonebook = new MainPage(admin);
        phonebook.setNavigation("Back to admin", () -> showWorkspace(adminPage, "ADMIN"));
        phonebook.setOnAccountDeleted(() -> cardLayout.show(mainPanel, "LOGIN"));
        showWorkspace(phonebook, "ADMIN_PHONEBOOK");
    }

    private void showWorkspace(Component workspace, String cardName) {
        if (activeWorkspace != null) mainPanel.remove(activeWorkspace);
        activeWorkspace = workspace;
        mainPanel.add(workspace, cardName);
        cardLayout.show(mainPanel, cardName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void resetPassword(String username, String email, char[] password, char[] confirmation) {
        BackgroundTask.run(this, () -> Validation.resetPassword(username, email, password, confirmation), error -> {
            clear(password);
            clear(confirmation);
            passwordResetPage.clearPasswords();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Password reset",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Your password has been updated. You can log in now.",
                    "Password updated", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "LOGIN");
        }, ex -> {
            clear(password);
            clear(confirmation);
            passwordResetPage.clearPasswords();
            showDatabaseError(ex);
        });
    }

    private void showDatabaseError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Could not complete the database operation: " + ex.getMessage(),
                "PhoneBook", JOptionPane.ERROR_MESSAGE);
    }

    private static void clear(char[] value) {
        if (value != null) java.util.Arrays.fill(value, '\0');
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
            dashboard.initializeApplication();
        });
    }
}
