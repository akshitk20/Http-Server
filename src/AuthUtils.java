import java.util.Base64;

public class AuthUtils {

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "password";

    public static String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public static boolean isValidCredentials(String encodedCredentials) {
        String validCredentials = encodeCredentials(USERNAME, PASSWORD);
        return validCredentials.equals(encodedCredentials);
    }
}
