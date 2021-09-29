package com.shinkamon.userlogin.support;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Support class to generate a hashed String; also provides a method to generate a random salt,
 * which is required when generating a hash to increase security against rainbow tables.
 */
public class HashGenerator {
    private final SecureRandom random;

    /**
     * Instantiates a new HashGenerator.
     * @param random a SecureRandom that is used to generate random salts for hashes.
     */
    public HashGenerator(SecureRandom random) {
        this.random = random;
    }
    /**
     * Helper method to convert a generated hash from byte[] to a String.
     * @param input a generated hash.
     * @return a String of the hash in hexadecimal form.
     */
    private String getHexString(final byte[] input) {
        BigInteger num = new BigInteger(1, input);
        return num.toString(16);
    }

    /**
     * Returns a randomly generated salt.
     * @return randomly generated salt as a String.
     */
    public String getRandomSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new String(salt, StandardCharsets.UTF_8);
    }

    /**
     * Returns a hash of the input and salt using the SHA-512 algorithm.
     * @param input user input, typically a password.
     * @param salt a randomized salt to increase security.
     * @return a hashed String of the input and salt.
     */
    public String getSHA512Hash(final char[] input, final String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA512");
            byte[] digest;

            // add the salt and then generate the hashed password
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            digest = md.digest(new String(input).getBytes(StandardCharsets.UTF_8));

            return getHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
