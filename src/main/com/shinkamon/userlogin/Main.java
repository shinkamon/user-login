package com.shinkamon.userlogin;

import com.shinkamon.userlogin.database.Database;
import com.shinkamon.userlogin.database.UserLogin;

import java.io.IOException;

/**
 * Main class that demonstrates user login system.
 */
public class Main {
    /**
     * Main method for user login.
     * @param args is not used.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(final String... args) throws IOException {
        Database.INSTANCE.setupDatabase(true);
        UserLogin.login();
    }
}
