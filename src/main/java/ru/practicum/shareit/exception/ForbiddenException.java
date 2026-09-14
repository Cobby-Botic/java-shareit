package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
        log.info(message);
    }
}
