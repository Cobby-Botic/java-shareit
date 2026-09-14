package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateException extends RuntimeException {
    public ValidateException(String message) {
        super(message);
        log.info(message);
    }
}
