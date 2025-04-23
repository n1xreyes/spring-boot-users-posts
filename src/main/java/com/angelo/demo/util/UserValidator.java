package com.angelo.demo.util;

import com.angelo.demo.common.dto.UserAndPostsDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UserAndPostsDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserAndPostsDto user = (UserAndPostsDto) target;
        if (null == user.getUserName()) {
            errors.rejectValue("username", "username.required", "Username is required");
        }

        if (null == user.getFullName()) {
            errors.rejectValue("fullName","fullName.required", "Full Name is required");
        }
    }
}
