package com.codewithmosh.store.common;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFound extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
