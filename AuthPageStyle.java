import javax.swing.*;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/** Shared CSS-inspired styling for the login and signup Swing pages. */
final class AuthPageStyle {
    static final Color NAVY = new Color(18, 39, 67);
    static final Color TEAL = new Color(22, 119, 120);
    static final Color INK = new Color(32, 49, 69);
    private static final Color CANVAS = new Color(241, 245, 249);

    private AuthPageStyle() { }

    static JPanel createCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 229, 238)),
                BorderFactory.createEmptyBorder(28, 32, 24, 32)));
        return card;
    }

    static JPanel createPage(String brandMessage, String brandDescription, JPanel card) {
        JPanel page = new JPanel(new BorderLayout(28, 0));
        page.setBackground(CANVAS);
        page.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));
        page.add(createBrandPanel(brandMessage, brandDescription), BorderLayout.WEST);

        JPanel cardHost = new JPanel(new GridBagLayout());
        cardHost.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        cardHost.add(card, gbc);
        page.add(cardHost, BorderLayout.CENTER);
        return page;
    }

    private static JPanel createBrandPanel(String message, String description) {
        JPanel brand = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), getHeight(), TEAL));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(34, 30, 30, 30));
        brand.setPreferredSize(new Dimension(330, 440));

        JLabel logo = new JLabel();
        Image phonebookImage = loadPhonebookImage();
        if (phonebookImage != null) {
            logo.setIcon(new ImageIcon(phonebookImage.getScaledInstance(72, 72, Image.SCALE_SMOOTH)));
        }
        logo.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(logo);
        brand.add(Box.createVerticalStrut(25));

        JLabel brandName = new JLabel("PhoneBook");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 18));
        brandName.setForeground(new Color(210, 232, 243));
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(16));

        JLabel headline = new JLabel("<html>" + message + "</html>");
        headline.setFont(new Font("SansSerif", Font.BOLD, 30));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(headline);
        brand.add(Box.createVerticalStrut(14));

        JLabel supportingText = new JLabel("<html><div style='width:230px'>" + description + "</div></html>");
        supportingText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        supportingText.setForeground(new Color(214, 228, 239));
        supportingText.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(supportingText);
        brand.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("PRIVATE  ·  PERSONAL  ·  ORGANIZED");
        footer.setFont(new Font("SansSerif", Font.BOLD, 9));
        footer.setForeground(new Color(195, 224, 226));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(footer);
        return brand;
    }

    static JLabel createTitle(String title, String subtitle) {
        JLabel label = new JLabel("<html><div style='text-align:center'>" + title
                + "<br><span style='font-size:12px;font-weight:normal;color:#68788B'>"
                + subtitle + "</span></div></html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(NAVY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    static void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(INK);
        field.setBackground(new Color(248, 250, 252));
        field.setCaretColor(TEAL);
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 231)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
        field.setBorder(border);
        field.setPreferredSize(new Dimension(280, 40));
    }

    static void stylePrimaryButton(JButton button) {
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(190, 42));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static Image loadPhonebookImage() {
        try {
            URL imageUrl = AuthPageStyle.class.getResource("/phonebook.png");
            if (imageUrl != null) return ImageIO.read(imageUrl);
            File imageFile = new File("phonebook.png");
            if (imageFile.isFile()) return ImageIO.read(imageFile);
        } catch (IOException ex) {
            System.err.println("Could not load phonebook.png: " + ex.getMessage());
        }
        return null;
    }
}
