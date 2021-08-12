package com.shinkamon.userlogin.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shinkamon.userlogin.support.InputReader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that provides access to a database for registered users. Access is provided through the
 * public field INSTANCE.
 */
public final class Database {
    /**
     * The single instance of the class, through which methods are accessible.
     */
    public static final Database INSTANCE = new Database();
    private String name;
    private String url;

    private Database() {
        Path path = Paths.get("resources/database-info.json");
        Gson gson = new Gson();
        try {
            String json = Files.readString(path);
            // get the type to convert the json into in order to avoid type-erasure
            Type stringMap = new TypeToken<HashMap<String, String>>() { }.getType();
            Map<String, String> databaseInfo = gson.fromJson(json, stringMap);
            name = databaseInfo.get("name");
            url = databaseInfo.get("url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to print the names of the tables in the database, and the number of rows in each table.
     */
    private void printTablesRowCount() {
        try (Connection connection = getConnection()) {
            // get the names of all tables in the database
            Statement statement = connection.createStatement();
            String query = """
                    SELECT name
                    FROM sqlite_master
                    WHERE type = 'table'
                    AND name NOT LIKE 'sqlite_%'
                    """;
            ResultSet tables = statement.executeQuery(query);

            System.out.println(name + " has the following table(s):");

            while (tables.next()) {
                // get the number of rows from the table
                String tableName = tables.getString(1);
                String query2 = "SELECT COUNT(*) FROM " + tableName;
                Statement statement2 = connection.createStatement();
                int rowCount = statement2.executeQuery(query2).getInt(1);

                System.out.println("  " + tableName + " (" + rowCount + " rows)");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create a new database, and add a table for storing user login details.
     */
    private void createDatabase() {
        // a new database is created when connecting if it doesn't already exist
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            // create the table for usernames and passwords
            String query = """
                  CREATE TABLE users (
                  username VARCHAR,
                  password_hash VARCHAR,
                  password_salt VARCHAR
                  )
                  """;
            statement.executeUpdate(query);
            System.out.println("Created new database " + name + ".");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Helper method to delete the database.
     */
    private void deleteDatabase() {
        File databaseFile = new File("resources/" + name);
        if (databaseFile.delete()) {
            System.out.println("Deleted database " + name + ".");
        } else {
            System.out.println("Unable to delete database " + name);
        }
    }

    /**
     * Returns an established connection to the database.
     * @return a connection to the database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }


    /**
     * Creates a new database if one doesn't already exist. If a database already exists, info about its tables
     * will be printed, and the user will be given the opportunity to recreate a new empty database.
     */
    public void setupDatabase() throws IOException {
        File databaseFile = new File("resources/" + name);

        if (!databaseFile.exists()) {
            createDatabase();
            return;
        }

        System.out.println("Database " + name + " already exists.");
        printTablesRowCount();
        System.out.print("Do you want to delete and recreate it from a template? Y/N: ");

        if (InputReader.readLine().equalsIgnoreCase("y")) {
            deleteDatabase();
            createDatabase();
        } else {
            System.out.println("Continuing with existing database.");
        }
    }
}
