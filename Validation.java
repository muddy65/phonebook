import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Input validation and password hashing for account and contact operations. */
public final class Validation {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int HASH_ITERATIONS = 210000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9 ()-]{5,19}$");

    private Validation() { }

    /** Creates the reserved administrator once, and only while the account table is empty. */
    public static String createInitialAdmin(String name, String address, String email,
                                            String phoneNumber, char[] password,
                                            char[] confirmation) throws SQLException {
        if (!DatabaseHelper.isUserTableEmpty()) {
            return "Initial administrator setup has already been completed.";
        }
        if (isBlank(name) || isBlank(address) || isBlank(email) || isBlank(phoneNumber)
                || password == null || confirmation == null) {
            return "Please complete every field.";
        }
        if (name.trim().length() > 100 || address.trim().length() > 200) {
            return "Name or address is too long.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches() || email.trim().length() > 254) {
            return "Enter a valid email address.";
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            return "Enter a valid phone number (digits, spaces, +, parentheses, or hyphens).";
        }
        String passwordError = validatePassword(password);
        if (passwordError != null) return passwordError;
        if (!MessageDigest.isEqual(passwordBytes(password), passwordBytes(confirmation))) {
            return "The passwords do not match.";
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        DatabaseHelper.createUser(name.trim(), address.trim(), "muddy",
                email.trim().toLowerCase(), phoneNumber.trim(), salt, hashPassword(password, salt));
        return null;
    }

    /** Returns null on success, otherwise a user-friendly validation message. */
    public static String signUp(String name, String address, String username,
                                char[] password, char[] confirmation,
                                String email, String phoneNumber) throws SQLException {
        if (isBlank(name) || isBlank(address) || isBlank(username)
                || isBlank(email) || isBlank(phoneNumber)
                || password == null || confirmation == null) {
            return "Please complete every field.";
        }
        if (name.trim().length() > 100 || address.trim().length() > 200
                || username.trim().length() < 3 || username.trim().length() > 40) {
            return "Name/address are too long, or username must be 3–40 characters.";
        }
        if (username.trim().equalsIgnoreCase("muddy")) {
            return "That username is reserved for the administrator.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches() || email.trim().length() > 254) {
            return "Enter a valid email address.";
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            return "Enter a valid phone number (digits, spaces, +, parentheses, or hyphens).";
        }
        String passwordError = validatePassword(password);
        if (passwordError != null) return passwordError;
        if (!MessageDigest.isEqual(passwordBytes(password), passwordBytes(confirmation))) {
            return "The passwords do not match.";
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = hashPassword(password, salt);
        try {
            DatabaseHelper.createUser(name.trim(), address.trim(), username.trim(),
                    email.trim().toLowerCase(), phoneNumber.trim(), salt, hash);
        } catch (SQLException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (message.contains("users.username")) return "That username is already in use.";
            if (message.contains("users.email")) return "That email is already registered.";
            if (message.contains("users.phone_number")) return "That phone number is already registered.";
            throw ex;
        }
        return null;
    }

    /** Updates a password after the user matches both their username and email address. */
    public static String resetPassword(String username, String email, char[] password,
                                       char[] confirmation) throws SQLException {
        if (isBlank(username) || isBlank(email) || password == null || confirmation == null) {
            return "Complete all reset fields.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Enter a valid email address.";
        String passwordError = validatePassword(password);
        if (passwordError != null) return passwordError;
        if (!MessageDigest.isEqual(passwordBytes(password), passwordBytes(confirmation))) {
            return "The passwords do not match.";
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = hashPassword(password, salt);
        if (!DatabaseHelper.updatePassword(username.trim(), email.trim(), salt, hash)) {
            return "We couldn't verify that username and email combination.";
        }
        return null;
    }

    public static LoginResult logIn(String username, char[] password) throws SQLException {
        if (isBlank(username) || password == null || password.length == 0 || password.length > 128) {
            return new LoginResult(null, "Enter your username and password.");
        }
        DatabaseHelper.UserAccount user = DatabaseHelper.findUserByUsername(username.trim());
        if (user == null || !MessageDigest.isEqual(
                hashPassword(password, user.passwordSalt), user.passwordHash)) {
            return new LoginResult(null, "Incorrect username or password.");
        }
        return new LoginResult(user, null);
    }

    public static String validateContact(String name, String phoneNumber, String address,
                                         String relationship, String otherNumber) {
        if (isBlank(name) || isBlank(phoneNumber) || isBlank(address)
                || isBlank(relationship)) {
            return "Please complete every contact field.";
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()
        /*|| !PHONE_PATTERN.matcher(otherNumber.trim()).matches()*/) {
            return "Enter valid phone numbers for both number fields.";
        }
        if (name.trim().length() > 100 || address.trim().length() > 200
                || relationship.trim().length() > 80) {
            return "One or more contact fields are too long.";
        }
        return null;
    }

    private static byte[] hashPassword(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing is unavailable.", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private static String validatePassword(char[] password) {
        if (password.length < 6) return "Password must be at least 6 characters.";
        if (password.length > 128) return "Password must be 128 characters or fewer.";
        return null;
    }

    private static byte[] passwordBytes(char[] password) {
        byte[] bytes = new byte[password.length * 2];
        for (int i = 0; i < password.length; i++) {
            bytes[i * 2] = (byte) (password[i] >>> 8);
            bytes[i * 2 + 1] = (byte) password[i];
        }
        return bytes;
    }

    private static boolean hasUppercase(char[] value) {
        for (char ch : value) if (Character.isUpperCase(ch)) return true;
        return false;
    }

    private static boolean hasLowercase(char[] value) {
        for (char ch : value) if (Character.isLowerCase(ch)) return true;
        return false;
    }

    private static boolean hasDigit(char[] value) {
        for (char ch : value) if (Character.isDigit(ch)) return true;
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class LoginResult {
        public final DatabaseHelper.UserAccount user;
        public final String error;

        private LoginResult(DatabaseHelper.UserAccount user, String error) {
            this.user = user;
            this.error = error;
        }
    }
}
