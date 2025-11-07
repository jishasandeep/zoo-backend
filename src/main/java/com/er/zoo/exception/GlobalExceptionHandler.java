package com.er.zoo.exception;

import com.er.zoo.logging.LoggerService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Global exception handler for Zoo API.
 * <p>
 * This class handles exceptions thrown across all controllers,
 * providing consistent HTTP responses and logging.
 * </p>
 * <p>
 * All exceptions are intercepted and transformed into JSON responses
 * containing the error details. Custom logging is performed via {@link LoggerService}.
 * </p>
 *
 * @author Jisha Badi
 * @version 1.0
 * @since 2025-11-07
 */
@ControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
    private final LoggerService logger;
    public static final String ENTITY_NAME = "ZooAPI";

    public GlobalExceptionHandler(LoggerService logger) {
        this.logger = logger;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException ex) {
        logger.error(ENTITY_NAME,"BAD_REQUEST","Missing request header: " + ex.getHeaderName(),ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", ZonedDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Missing required header",
                        "message", "Header '" + ex.getHeaderName() + "' is required"
                ));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<Map<String,Object>> handleDuplicateRequest(DuplicateRequestException ex) {
        logger.warn(ENTITY_NAME, "DUPLICATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(getExceptionDetails(ex));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleBadRequest(IllegalArgumentException ex) {
        logger.warn(ENTITY_NAME, "BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getExceptionDetails(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (a, b)->a+", "+b));
        var body = new HashMap<String,Object>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("errors", errors);
        logger.warn(ENTITY_NAME, "BAD_REQUEST", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleDuplicateRequest(ResponseStatusException ex) {
        logger.warn(ENTITY_NAME, "DUPLICATE", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(getExceptionDetails(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {

        var body = new HashMap<String,Object>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("error", "Internal server error");
        body.put( "detail", ex.getMessage());
        logger.error(ENTITY_NAME, "INTERNAL_ERROR", "Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
