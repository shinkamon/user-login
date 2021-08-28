This project is based on [this blog post][link to blog post] by Robert Heaton.
It's a simple login system that stores user credentials in a database.

What does the program do?
  - Sets up a default database if one doesn't already exist. Can optionally ask the user if they want to recreate a new,
    empty database from a default template, by setting the *recreate* parameter of `setupDatabase()` to true.
  - Asks the user if they want to register as a new user, prompting them to enter a new username and password.
  - Lets the user log in by comparing the username and password entered by the user to the credentials stored in the 
    database.

Some additional points of interest include:
  - Passwords are hashed together with a randomly generated salt for added security.
  - Passwords are masked when the program is run from a console. When running from an IDE, password input will still
    work, but the passwords won't be masked. This is because [Console#readPassword][readPassword doc] cannot be run from
    within an IDE since the console from `System.console()` will be null in that case.
  - When registering a new user, the entered username and password will be validated to make sure they conform to a
    predetermined format.
  - All database queries use precompiled SQL statements to guard against SQL injection.
  
[link to blog post]: https://robertheaton.com/2019/08/12/programming-projects-for-advanced-beginners-user-logins/
[readPassword doc]: https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/io/Console.html#readPassword()