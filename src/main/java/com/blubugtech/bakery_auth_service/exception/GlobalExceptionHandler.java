package com.blubugtech.bakery_auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.exception.handler.BaseExceptionHandler;
import org.blubakery.bakery_common_libs.exception.handler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("USER_NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex, WebRequest request) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("INVALID_CREDENTIALS", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(AccountLockedException ex, WebRequest request) {
        log.error("Account locked: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ACCOUNT_LOCKED", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, WebRequest request) {
        log.error("Auth error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("AUTH_ERROR", ex.getMessage(), LocalDateTime.now(), request.getDescription(false)));
    }
}

