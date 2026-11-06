package com.educenter.app.utils;

import android.util.Base64;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility for hashing and verifying passwords securely using PBKDF2WithHmacSHA256.
 * Guarantees zero plaintext passwords in database.
 */
public final class PasswordUtils {

    private static final String TAG = "PasswordUtils";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtils() {
        // Utility class
    }

    /**
     * Generates a random cryptographic salt.
     *
     * @return Base64-encoded salt string.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Hashes a password using PBKDF2 with the provided salt.
     *
     * @param password Plaintext password
     * @param saltBase64 Base64-encoded salt
     * @return Base64-encoded password hash, or empty string on error.
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null || saltBase64 == null) {
            return "";
        }
        try {
            byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Error hashing password", e);
            return "";
        }
    }

    /**
     * Verifies a candidate password against an existing hash and salt.
     *
     * @param candidatePassword Plaintext password attempt
     * @param storedHash Stored Base64 hash
     * @param storedSalt Stored Base64 salt
     * @return True if match, false otherwise.
     */
    public static boolean verifyPassword(String candidatePassword, String storedHash, String storedSalt) {
        if (candidatePassword == null || storedHash == null || storedSalt == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword, storedSalt);
        return storedHash.equals(candidateHash);
    }
}
