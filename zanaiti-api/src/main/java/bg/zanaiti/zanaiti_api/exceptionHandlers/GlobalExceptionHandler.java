package bg.zanaiti.zanaiti_api.exceptionHandlers;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Specific case - duplicate craft
    @ExceptionHandler(DuplicateCraftException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCraft(DuplicateCraftException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // 409 Conflict
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    // Specific case - duplicate question
    @ExceptionHandler(DuplicateQuestionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateQuestion(DuplicateQuestionException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // 409 Conflict
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    // Handle validation errors (e.g., @NotBlank, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : fieldErrors) {
            String field = error.getField();
            String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation failed";
            errors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
        }

        ValidationErrorResponse ver = new ValidationErrorResponse(
                "Validation failed",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(ver);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleEnumConversionError(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Invalid value provided: " + ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), LocalDateTime.now()));
    }

    // Handle other exceptions (optional)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
        ex.printStackTrace(); // debugging helper
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse("An unexpected error occurred: " + ex.getMessage(), LocalDateTime.now()));
    }
}