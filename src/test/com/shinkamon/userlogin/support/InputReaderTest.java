package com.shinkamon.userlogin.support;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class InputReaderTest {

    @Test
    void readsLineCorrectly() throws IOException {
        String input = "Test string.";
        InputStream standardInput = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            assertEquals("Test string.", InputReader.readLine());
        } finally {
            System.setIn(standardInput);
        }
    }

    @Test
    void readsPasswordCorrectly() throws IOException {
        String input = "Password1";
        InputStream standardInput = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            assertArrayEquals(new char[] {'P', 'a', 's', 's' , 'w', 'o', 'r', 'd', '1'},
                    InputReader.readPassword());
        } finally {
            System.setIn(standardInput);
        }
    }
}