package com.shinkamon.userlogin.database;

import com.shinkamon.userlogin.support.Hash;
import com.shinkamon.userlogin.support.InputReader;

import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Helper method that checks whether given input matches to given regular expression.
     * @param input the String to check.
     * @param regex the regular expression to match against as a String.
     * @return whether the input matches the regular expression as a boolean.
     */
    private static boolean validateInput(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }

    /**
     * Helper method to read a new username from the console. Will repeatedly ask for input until a valid
     * username is given.
     * @return a valid username as a String.
     * @throws IOException if an I/O error occurs.
     */
    private static String readNewUsername() throws IOException {
        String username;
        // can only contain alphanumeric characters and underscores, and must be between 3-30 characters long
        String regex = "^\\w{3,30}$";

        while (true) {
            System.out.print("  username: ");
            username = InputReader.readLine();

            if (validateInput(username, regex)) {
                return username;
            }

            System.out.println("Invalid username. Can only contain alphanumeric characters and underscores, ");
            System.out.println("and must be between 3 and 30 characters long.");
        }
    }

    /**
     * Helper method to read a new password from the console. Will repeatedly ask for input until a valid
     * password is given.
     * @return a valid password as a char[].
     * @throws IOException if an I/O error occurs.
     */
    private static char[] readNewPassword() throws IOException {
        char[] password;
        // (?=.*[a-zA-z]) must contain at least one letter
        // (?=.*\d) must contain at least one digit
        // (?=\S+$) must contain only non-whitespace characters
        // .{8,50} must be between 8-50 characters long
        String regex = "^(?=.*[a-zA-z])(?=.*\\d)(?=\\S+$).{8,50}$";

        while (true) {
            System.out.print("  password: ");
            // since System.console doesn't work in an IDE we can't just use Console#readPassword()
            // instead we use our own reader to account for this while still masking input when run from a console
            password = InputReader.readPassword();

            if (validateInput(new String(password), regex)) {
                return password;
            }

            System.out.println("Invalid password. Must contain at least one letter and one digit,");
            System.out.println("cannot contain any whitespaces, and must be between 8 and 50 characters long.");
        }
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
            if (hasUser(username, connection)) {
                System.out.println("That username is already taken.");
                return false;
            }

            String query = "INSERT INTO users VALUES(?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, passwordSalt);
            statement.executeUpdate();

            System.out.println("New user registered.");
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Failed to register user.");
        return false;
    }

    /**
     * Helper method to register a new user.
     * @throws IOException if an I/O error occurs.
     */
    private static void addNewUser() throws IOException {
        String username;
        String passwordHash;
        String passwordSalt = Hash.getRandomSalt();

        do {
            System.out.println("Enter a new username and password to register.");
            username = readNewUsername();
            passwordHash = Hash.getSHA512Hash(readNewPassword(), passwordSalt);

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
