package com.angelo.demo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {

//    @Test
//    void constructorIsPrivate() throws Exception {
//        java.lang.reflect.Constructor<EmailValidator> constructor = EmailValidator.class.getDeclaredConstructor();
//        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
//        constructor.setAccessible(true);
//        assertNotNull(constructor.newInstance());
//    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "test.test@example.co.uk",
            "test+alias@example-domain.net",
            "test_user@example.org",
            "_______@example.com",
            "firstname.lastname@example.com",
            "email@subdomain.example.com",
            "1234567890@example.com",
            "email@example-one.com",
            "email@example.name",
            "email@example.museum",
            "email@example.co.jp"
    })
    void testValidEmails(String email) {
        assertTrue(EmailValidator.validateEmail(email), "Expected valid: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email.@example.com",
            "email..email@example.com",
            "email@example.com (Joe Smith)",
            "email@example",
            "email@111.222.333.44445",
            "email@example.c",
            "email@[123.123.123.123]",
            "\"email\"@example.com"
    })
    void testInvalidEmails(String email) {
        assertFalse(EmailValidator.validateEmail(email), "Expected invalid: " + email);
    }

//    @Test
//    void testNullEmail() {
//        assertFalse(EmailValidator.validateEmail(null), "Expected invalid for null email");
//    }

    @Test
    void testEmptyEmail() {
        assertFalse(EmailValidator.validateEmail(""), "Expected invalid for empty email");
    }
}
