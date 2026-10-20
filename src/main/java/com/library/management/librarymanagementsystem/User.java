package com.library.management.librarymanagementsystem;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * User.java
 *
 * Defines a User object to be used for storing user information in Library Management System.
 * The password itself is never stored: only a random salt and a PBKDF2 hash of the password with
 * it, so reading users.csv does not reveal anyone's password. A user's checked-out books are not
 * kept here; each book records its borrower (see Book).
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class User {
    static final List<String> HEADER = List.of("username", "iterations", "salt", "hash", "admin");
    /** OWASP's 2023 figure for PBKDF2 with HMAC-SHA256. */
    static final int ITERATIONS = 600_000;

    private final String username;
    private final int iterations;
    private final String salt;
    private final String hash;
    private boolean admin;

    private User(String username, int iterations, String salt, String hash, boolean admin) {
        this.username = username;
        this.iterations = iterations;
        this.salt = salt;
        this.hash = hash;
        this.admin = admin;
    }

    /**
     * A new account. The password is hashed here and then forgotten.
     * @param username username tied with user, in lower case
     * @param password password chosen by the user
     * @param admin is permissions for user admin
     */
    static User create(String username, String password, boolean admin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String s = Base64.getEncoder().encodeToString(salt);
        return new User(username, ITERATIONS, s, hash(password, salt, ITERATIONS), admin);
    }

    /** Whether this is the user's password. */
    boolean checkPassword(String password) {
        String attempt = hash(password, Base64.getDecoder().decode(salt), iterations);
        return MessageDigest.isEqual(attempt.getBytes(), hash.getBytes());
    }

    private static String hash(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is missing from this Java runtime", e);
        }
    }

    //Getter Methods
    public String getUsername() { return username; }
    public boolean getAdmin() { return admin; }

    //Setter Methods
    public void setAdmin(boolean newAdmin) { this.admin = newAdmin; }

    /** The user's fields, in the order of {@link #HEADER}. */
    List<String> toRow() {
        return List.of(username, Integer.toString(iterations), salt, hash, Boolean.toString(admin));
    }

    /** A user from one row of users.csv, or null if the row is not one. */
    static User fromRow(List<String> row) {
        if (row.size() != HEADER.size()) return null;
        try {
            return new User(row.get(0), Integer.parseInt(row.get(1)), row.get(2), row.get(3), Boolean.parseBoolean(row.get(4)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
