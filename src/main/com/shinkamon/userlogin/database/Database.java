package com.shinkamon.userlogin.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 */
public class Database {
    public static final Database INSTANCE = new Database();
    private String name;
    private String url;

    private Database() {
        Path path = Paths.get("resources/dbinfo.json");
        Gson gson = new Gson();
        try {
            String json = Files.readString(path);
            // get the type to convert the json into in order to avoid type-erasure
            Type stringMap = new TypeToken<HashMap<String, String>>() { }.getType();
            Map<String, String> dbInfo = gson.fromJson(json, stringMap);
            name = dbInfo.get("name");
            url = dbInfo.get("url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void printTablesRowCount() {
        try (Connection connection = getConnection()) {
            // get the names of all tables in the db
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
     *
     */
    private void createDB() {
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
     *
     */
    private void deleteDB() {
        File dbFile = new File("resources/" + name);
        if (dbFile.delete()) {
            System.out.println("Deleted database " + name + ".");
        } else {
            System.out.println("Unable to delete database " + name);
        }
    }

    /**
     *
     * @return
     */
    public Connection getConnection() {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return connection;
    }


    /**
     *
     */
    public void setupDB() {
        File dbFile = new File("resources/" + name);

        if (dbFile.exists()) {
            Scanner in = new Scanner(System.in);

            System.out.println("Database " + name + " already exists.");
            printTablesRowCount();
            System.out.print("Do you want to delete and recreate it from a template? Y/N: ");

            if (in.nextLine().equalsIgnoreCase("y")) {
                deleteDB();
                createDB();
            } else {
                System.out.println("Continuing with existing database.");
            }

        } else {
            createDB();
        }
    }

}
