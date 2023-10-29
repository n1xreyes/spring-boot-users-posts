package com.angelo.demo.exception;

import com.angelo.demo.entity.User;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;

@AllArgsConstructor
public class UserNotFoundException extends Exception{
    final transient User user;

    public static UserNotFoundException createWith(User user) {
        return new UserNotFoundException(user);
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("User with userId {0} not found", user.getId());
    }
}
