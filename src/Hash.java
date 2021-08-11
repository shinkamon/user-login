import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Hash {
    private static String getHexString(final byte[] input) {
        BigInteger num = new BigInteger(1, input);
        return num.toString(16);
    }

    public static String getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return new String(salt);
    }

    public static String getSHA512Hash(final String input, final String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA512");
            byte[] digest;

            // add the salt and then generate the hashed password
            md.update(salt.getBytes());
            digest = md.digest(input.getBytes());

            return getHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
