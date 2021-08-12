package com.shinkamon.userlogin;

import com.shinkamon.userlogin.database.Database;
import com.shinkamon.userlogin.database.UserLogin;

public class Main {
    public static void main(final String... args) {
        Database.INSTANCE.setupDB();
        UserLogin.login();
    }
}
