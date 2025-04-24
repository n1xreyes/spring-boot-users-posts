package com.angelo.demo.util;

import com.angelo.demo.common.dto.UserAndPostsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTest {
    private UserValidator userValidator;
    private UserAndPostsDto userDto;
    private Errors errors;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator();
        userDto = new UserAndPostsDto();
        errors = new BeanPropertyBindingResult(userDto, "userDto");
    }

    @Test
    void supports_shouldReturnTrueForUserAndPostsDto() {
        assertTrue(userValidator.supports(UserAndPostsDto.class));
    }

    @Test
    void supports_shouldReturnFalseForOtherClass() {
        assertFalse(userValidator.supports(Object.class));
    }

    @Test
    void validate_shouldPassWithValidUser() {
        userDto.setUserName("testuser");
        userDto.setFullName("Test User");

        userValidator.validate(userDto, errors);

        assertFalse(errors.hasErrors());
    }

//    @Test
//    void validate_shouldFailWhenUsernameIsNull() {
//        userDto.setFullName("Test User");
//        // userDto.setUserName(null);
//
//        userValidator.validate(userDto, errors);
//
//        assertTrue(errors.hasErrors());
//        assertEquals(1, errors.getFieldErrorCount("username"));
//        assertEquals("username.required", errors.getFieldError("username").getCode());
//    }

    @Test
    void validate_shouldFailWhenFullNameIsNull() {
        userDto.setUserName("testuser");
        // userDto.setFullName(null); // Implicitly null

        userValidator.validate(userDto, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getFieldErrorCount("fullName"));
        assertEquals("fullName.required", errors.getFieldError("fullName").getCode());
    }

//    @Test
//    void validate_shouldFailWhenBothAreNull() {
//        // userDto.setUserName(null);
//        // userDto.setFullName(null);
//
//        userValidator.validate(userDto, errors);
//
//        assertTrue(errors.hasErrors());
//        assertEquals(2, errors.getErrorCount());
//        assertNotNull(errors.getFieldError("username"));
//        assertNotNull(errors.getFieldError("fullName"));
//    }
}
