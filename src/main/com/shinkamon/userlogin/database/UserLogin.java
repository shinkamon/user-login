package com.shinkamon.userlogin.database;

import com.shinkamon.userlogin.support.Hash;
import com.shinkamon.userlogin.support.InputReader;

import java.io.IOException;
import java.sql.*;

/**
 * Handles user login using static methods.
 * Enables the registration of new users as well as authenticating existing users.
 */
public class UserLogin {
    /**
     * Helper method to check whether a username and a hashed password are valid.
     * E.g. the username belongs to a registered user and the hashed password is the
     * correct password associated with that username.
     * @param username the username to validate.
     * @param passwordHash the hashed password to validate.
     * @return whether the credentials are valid or not as a boolean.
     */
    private static boolean isValidCredentials(final String username, final String passwordHash) {
        try (Connection connection = Database.INSTANCE.getConnection()) {
            if (hasUser(username, connection)) {
                String query = "SELECT password_hash FROM users WHERE username = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();

                return resultSet.getString(1).equals(passwordHash);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper method that checks whether a username is registered. E.g. checks whether the username is
     * in the database. Takes an already established {@link java.sql.Connection} to the database as a
     * parameter to avoid opening a new connection.
     * @param username the username to check.
     * @param connection a connection to the database.
     * @return whether the username exists or not as a boolean.
     * @throws SQLException if a database access error occurs.
     */
    private static boolean hasUser(final String username, final Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        int count = statement.executeQuery().getInt(1);

        return count > 0;
    }

    /**
     * Helper method to try to add a new user to the database. Will fail if a user with the supplied username
     * is already registered in the database.
     * @param username the username to register.
     * @param passwordHash the hashed password to register.
     * @param passwordSalt the salt needed to generate the hashed password.
     * @return whether the user was successfully added or not as a boolean.
     */
    private static boolean addUserToDatabase(final String username, final String passwordHash,
                                             final String passwordSalt) {
        try (Connection connection = Database.INSTANCE.getConnection()) {
            if (!hasUser(username, connection)) {
                String query = "INSERT INTO users VALUES(?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, passwordHash);
                statement.setString(3, passwordSalt);
                statement.executeUpdate();

                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper method to register a new user.
     * @throws IOException if an I/O error occurs.
     */
    private static void addNewUser() throws IOException {
        String username;
        String passwordHash;
        String passwordSalt = Hash.getSalt();

        do {
            System.out.println("Enter a new username and password to register.");
            System.out.print("  username: ");
            username = InputReader.readLine();
            System.out.print("  password: ");
            // since System.console doesn't work in an IDE we can't just use Console#readPassword()
            // instead we use our own reader to account for this while still masking input when run from a console
            passwordHash = Hash.getSHA512Hash(InputReader.readPassword(), passwordSalt);
        } while (!addUserToDatabase(username,  passwordHash, passwordSalt));
    }

    /**
     * Helper method to get the salt needed to generate the hashed password associated with the
     * supplied username. If no such username exists an empty String will be returned.
     * @param username the username associated with the salt.
     * @return the salt as a String, or an empty String if the supplied username doesn't exist.
     */
    private static String getSalt(final String username) {
        try (Connection connection = Database.INSTANCE.getConnection()) {
            if (hasUser(username, connection)) {
                String query = "SELECT password_salt FROM users WHERE username = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();

                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Enables a user to log in by entering their username and password.
     * Also allows for the registration of new users.
     * @throws IOException if an I/O error occurs.
     */
    public static void login() throws IOException {
        String username;
        String passwordHash;

        System.out.print("Would you like a register a new user? Y/N: ");
        if (InputReader.readLine().equalsIgnoreCase("y")) {
            addNewUser();
        }

        System.out.println("Enter your username and password to log in.");
        System.out.print("  username: ");
        username = InputReader.readLine();
        System.out.print("  password: ");
        passwordHash = Hash.getSHA512Hash(InputReader.readPassword(), getSalt(username));

        if (isValidCredentials(username, passwordHash)) {
            System.out.println("Authenticated.");
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}
