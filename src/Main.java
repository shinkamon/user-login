
public class Main {
    public static void main(final String... args) {
        Database.INSTANCE.setupDB();
        UserLogin.login();
    }
}
