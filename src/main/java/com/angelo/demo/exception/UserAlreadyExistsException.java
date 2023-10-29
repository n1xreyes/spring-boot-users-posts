package com.angelo.demo.exception;

import com.angelo.demo.entity.User;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;

@AllArgsConstructor
public class UserAlreadyExistsException extends Exception {
    final transient User user;

    public static UserAlreadyExistsException createWith(User user) {
        return new UserAlreadyExistsException(user);
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("User already exists for userName {} and/or email {}", user.getUsername(), user.getEmail());
    }
}
