import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Local password recovery form. */
public class PasswordResetPage {
    private final JPanel contentPanel;
    private final JTextField usernameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JPasswordField newPasswordField = new JPasswordField(22);
    private final JPasswordField confirmPasswordField = new JPasswordField(22);
    private ResetAction resetAction;
    private Runnable onBackToLogin;

    public PasswordResetPage() {
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(239, 244, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 227, 238)),
                BorderFactory.createEmptyBorder(26, 30, 22, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 9, 8, 9);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Reset your password");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(new Color(21, 45, 75));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 4, 8);
        card.add(title, gbc);

        JLabel hint = new JLabel("Enter your account details and choose a new password.");
        hint.setForeground(new Color(96, 111, 130));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 16, 8);
        card.add(hint, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(7, 8, 7, 8);
        addField(card, gbc, 2, "Username:", usernameField);
        addField(card, gbc, 3, "Account email:", emailField);
        addField(card, gbc, 4, "New password:", newPasswordField);
        addField(card, gbc, 5, "Confirm password:", confirmPasswordField);

        JButton resetButton = new JButton("Save new password");
        resetButton.setBackground(new Color(22, 119, 120));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setPreferredSize(new Dimension(180, 38));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 8, 8, 8);
        card.add(resetButton, gbc);
        resetButton.addActionListener(event -> {
            if (resetAction != null) {
                resetAction.reset(usernameField.getText(), emailField.getText(),
                        newPasswordField.getPassword(), confirmPasswordField.getPassword());
            }
        });

        JLabel backLink = new JLabel("<html><a href='login'>Back to login</a></html>");
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(6, 8, 0, 8);
        card.add(backLink, gbc);
        backLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (onBackToLogin != null) onBackToLogin.run();
            }
        });

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.anchor = GridBagConstraints.CENTER;
        contentPanel.add(card, outer);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row,
                          String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setResetAction(ResetAction resetAction) {
        this.resetAction = resetAction;
    }

    public void setOnBackToLogin(Runnable onBackToLogin) {
        this.onBackToLogin = onBackToLogin;
    }

    public void clearPasswords() {
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    public interface ResetAction {
        void reset(String username, String email, char[] password, char[] confirmation);
    }
}
