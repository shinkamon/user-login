package com.shinkamon.userlogin;

import com.shinkamon.userlogin.database.Database;
import com.shinkamon.userlogin.database.UserLogin;

import java.io.IOException;

//TODO: regex to only accept correct username and password formats
//TODO: update format and coloring of text
public class Main {
    /**
     * Main method for user login.
     * @param args is not used.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(final String... args) throws IOException {
        Database.INSTANCE.setupDatabase();
        UserLogin.login();
    }
}
