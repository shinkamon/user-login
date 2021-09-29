package com.shinkamon.userlogin.support;

import java.io.*;

/**
 * Static support class to read console input; uses {@link java.lang.System#console()} to mask
 * password input. However, since {@link java.lang.System#console()} is not available when running
 * from an IDE, an alternative reader is provided in such cases, but passwords will not be masked.
 */
public class InputReader {
    /**
     * Reads a line of text.
     * @return the read input as a String.
     * @throws IOException if an I/O error occurs.
     */
    public static String readLine() throws IOException {
        if (System.console() != null) {
            return System.console().readLine();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.readLine();
    }

    /**
     * Reads a password while masking the input. Will only mask the input when read from a console,
     * when read from an IDE the password will be fully visible.
     * @return the read input as a char[].
     * @throws IOException if an I/O error occurs.
     */
    public static char[] readPassword() throws IOException {
        if (System.console() != null) {
            return System.console().readPassword();
        }

        return readLine().toCharArray();
    }
}
