import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** MySQL access for user accounts and each account's private contact list. */
public final class DatabaseHelper {
    // Set these values for your local MySQL server.
    private static final String URL = "jdbc:mysql://localhost:3306/contacts";//"jdbc:mysql://3f4EcWQAsYeGxbg.root:T5v6Q4eoSdS4t809@gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/sys";
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/"; //"gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
    private static final String USERNAME = "muddy";//"3f4EcWQAsYeGxbg.root";
    private static final String PASSWORD = "@muddy";//"T5v6Q4eoSdS4t809";

    private DatabaseHelper() { }

    public static void initialize() throws SQLException, IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC Connector is missing. Add mysql-connector-j to the project libraries.", ex);
        }

        String schema;
        try (InputStream resource = DatabaseHelper.class.getResourceAsStream("/schema.sql")) {
            if (resource != null) {
                schema = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                Path schemaPath = Path.of("schema.sql");
                if (!Files.exists(schemaPath)) {
                    throw new IOException("schema.sql was not found in the classpath or working directory.");
                }
                schema = Files.readString(schemaPath, StandardCharsets.UTF_8);
            }
        }
        // Connect without selecting a database so schema.sql can create contacts first.
        try (Connection connection = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.trim().isEmpty()) {
                    statement.execute(sql.trim());
                }
            }
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void createUser(String name, String address, String username,
                                  String email, String phoneNumber,
                                  byte[] salt, byte[] passwordHash) throws SQLException {
        String sql = "INSERT INTO users "
                + "(name, address, username, password_salt, password_hash, email, phone_number) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, username);
            statement.setString(4, Base64.getEncoder().encodeToString(salt));
            statement.setString(5, Base64.getEncoder().encodeToString(passwordHash));
            statement.setString(6, email);
            statement.setString(7, phoneNumber);
            statement.executeUpdate();
        }
    }

    public static UserAccount findUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, name, address, username, password_salt, password_hash, email, phone_number "
                + "FROM users WHERE username = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                return new UserAccount(result.getLong("id"), result.getString("name"),
                        result.getString("address"), result.getString("username"),
                        Base64.getDecoder().decode(result.getString("password_salt")),
                        Base64.getDecoder().decode(result.getString("password_hash")),
                        result.getString("email"), result.getString("phone_number"));
            }
        }
    }

    public static List<UserSummary> getAllUsers() throws SQLException {
        List<UserSummary> users = new ArrayList<>();
        String sql = "SELECT id, name, address, username, email, phone_number, created_at "
                + "FROM users ORDER BY name";
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                users.add(new UserSummary(result.getLong("id"), result.getString("name"),
                        result.getString("address"), result.getString("username"),
                        result.getString("email"), result.getString("phone_number"),
                        result.getString("created_at")));
            }
        }
        return users;
    }

    public static boolean updatePassword(String username, String email,
                                         byte[] salt, byte[] passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_salt = ?, password_hash = ? "
                + "WHERE username = ? AND email = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Base64.getEncoder().encodeToString(salt));
            statement.setString(2, Base64.getEncoder().encodeToString(passwordHash));
            statement.setString(3, username.trim());
            statement.setString(4, email.trim());
            return statement.executeUpdate() == 1;
        }
    }

    public static void addContact(long userId, String name, String phoneNumber,
                                  String address, String relationship, String otherNumber)
            throws SQLException {
        String sql = "INSERT INTO contacts (user_id, name, phone_number, address, relationship, other_number) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, name);
            statement.setString(3, phoneNumber);
            statement.setString(4, address);
            statement.setString(5, relationship);
            statement.setString(6, otherNumber);
            statement.executeUpdate();
        }
    }

    public static List<Contact> getContacts(long userId) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT id, name, phone_number, address, relationship, other_number "
                + "FROM contacts WHERE user_id = ? ORDER BY name";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    contacts.add(new Contact(result.getLong("id"), result.getString("name"), result.getString("phone_number"),
                            result.getString("address"), result.getString("relationship"),
                            result.getString("other_number")));
                }
            }
        }
        return contacts;
    }

    public static boolean deleteContact(long userId, long contactId) throws SQLException {
        String sql = "DELETE FROM contacts WHERE id = ? AND user_id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, contactId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public static boolean deleteUser(long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public static final class UserAccount {
        public final long id;
        public final String name;
        public final String address;
        public final String username;
        public final byte[] passwordSalt;
        public final byte[] passwordHash;
        public final String email;
        public final String phoneNumber;

        private UserAccount(long id, String name, String address, String username,
                            byte[] passwordSalt, byte[] passwordHash,
                            String email, String phoneNumber) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.username = username;
            this.passwordSalt = passwordSalt;
            this.passwordHash = passwordHash;
            this.email = email;
            this.phoneNumber = phoneNumber;
        }
    }

    public static final class Contact {
        public final long id;
        public final String name;
        public final String phoneNumber;
        public final String address;
        public final String relationship;
        public final String otherNumber;

        private Contact(long id, String name, String phoneNumber, String address,
                        String relationship, String otherNumber) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.relationship = relationship;
            this.otherNumber = otherNumber;
        }
    }

    /** Account information safe to display in the admin user list. */
    public static final class UserSummary {
        public final long id;
        public final String name;
        public final String address;
        public final String username;
        public final String email;
        public final String phoneNumber;
        public final String createdAt;

        private UserSummary(long id, String name, String address, String username,
                            String email, String phoneNumber, String createdAt) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.username = username;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.createdAt = createdAt;
        }
    }
}
