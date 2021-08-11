import java.sql.*;
import java.util.Scanner;

public class UserLogin {
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

    private static boolean hasUser(final String username, final Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        int count = statement.executeQuery().getInt(1);

        return count > 0;
    }

    private static boolean addUserToDB(final String username, final String passwordHash, final String passwordSalt) {
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

    private static void addNewUser(Scanner in) {
        String username;
        String passwordHash;
        String passwordSalt = Hash.getSalt();

        do {
            System.out.println("Enter a new username and password to register.");
            System.out.print("  username: ");
            username = in.nextLine();
            System.out.print("  password: ");
            passwordHash = Hash.getSHA512Hash(in.nextLine(), passwordSalt);
        } while (!addUserToDB(username,  passwordHash, passwordSalt));
    }

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

    //TODO: change all password (and username? Salt?) variables in project to byte[] instead of string
    //TODO: use an abstract layer for Console to enable hidden password input:
    //http://illegalargumentexception.blogspot.com/2010/09/java-systemconsole-ides-and-testing.html
    //TODO: regex to only accept correct username and password formats
    //TODO: update format and coloring of text
    public static void login() {
        Scanner in = new Scanner(System.in);
        String username;
        String passwordHash;

        System.out.print("Would you like a register a new user? Y/N: ");
        if (in.nextLine().equalsIgnoreCase("y")) {
            addNewUser(in);
        }

        System.out.println("Enter your username and password to log in.");
        System.out.print("  username: ");
        username = in.nextLine();
        System.out.print("  password: ");
        passwordHash = Hash.getSHA512Hash(in.nextLine(), getSalt(username));

        if (isValidCredentials(username, passwordHash)) {
            System.out.println("Authenticated.");
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}
