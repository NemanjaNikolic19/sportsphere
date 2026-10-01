package rs.etf.pia.sportsphere.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private PasswordUtil() {
    }
    
    public static String sha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for(byte b: encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 algorithm not avaliable", e);
        }
    }

    public static boolean matches(String rawPassword, String storedPasswordHash){
        if(rawPassword == null || storedPasswordHash == null)
            return false;

        String rawPasswordHash = sha256(rawPassword);

        return rawPasswordHash.equalsIgnoreCase(storedPasswordHash);
    }
}
