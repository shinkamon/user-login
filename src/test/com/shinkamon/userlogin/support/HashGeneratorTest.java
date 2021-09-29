package com.shinkamon.userlogin.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class HashGeneratorTest {
    HashGenerator hashGenerator;

    @BeforeEach
    void setup() {
        this.hashGenerator = new HashGenerator(new SecureRandom());
    }

    @Test
    void randomSaltIsRandom() {
        String salt1 = hashGenerator.getRandomSalt();
        String salt2 = hashGenerator.getRandomSalt();

        assertNotEquals(salt1, salt2);
    }

    @Test
    void randomSaltIsNotEmpty() {
        String salt = hashGenerator.getRandomSalt();

        assertNotEquals(0, salt.length());
    }

    @Test
    void generatesCorrectSHA512Hash() {
        char[] input = new char[] {'t', 'e', 's', 't', 'I', 'n', 'p', 'u', 't'};
        String salt = "testSalt";
        String generatedHash = hashGenerator.getSHA512Hash(input, salt);
        // SHA512 hash from 'testSalttestInput'
        String hash = "124c78671010b5825b63adbfe3f4d7a134b76a11cdbbfb054bbd6b91caa601ef" +
                "e7f2316e11389ff90d5186ace73deeebd34c65d440ae200cf2b3ea861448b3d0";

        assertEquals(hash, generatedHash);
    }
}